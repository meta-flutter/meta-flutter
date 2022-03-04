#!/usr/bin/env python3
# Copyright (c) 2022 Joel Winarske. All rights reserved.
# Use of this source code is governed by a BSD-style license that can be
# found in the LICENSE file.
"""
converts gclient constructs to Yocto format include file.

Depends on gitpython - pip3 install gitpython

Usage:
* Navigate to folder with .glcient file, and issue
  python3 gclient_bitbake.py

  Default output gclient-entries.inc is written in the same folder

* Example usage:
  python3 gclient_bitbake.py -o=gclient-entries-1234.inc
"""

from collections import OrderedDict
from git.refs.tag import TagReference
from git.repo.base import Repo
from multiprocessing import Process, Queue, freeze_support, cpu_count

import errno
import optparse
import os
import re
import shutil
import subprocess
import sys
import tempfile
import time


def get_git_revision_hash() -> str:
  return subprocess.check_output(['git', 'rev-parse', 'HEAD']).decode('ascii').strip()

def get_git_revision_short_hash() -> str:
  return subprocess.check_output(['git', 'rev-parse', '--short', 'HEAD']).decode('ascii').strip()

def worker(input, output):
  for func, args in iter(input.get, 'STOP'):
    result = calculate(func, args)
    output.put(result)

def calculate(func, args):
  result = func(*args)
  return result

def get_branch(name, url, commitSHA):
  print('checking: ' + url + '@' + commitSHA)
  with tempfile.TemporaryDirectory() as tmpdirname:
    local = Repo.clone_from(url,tmpdirname)
    try:
      branch = local.git.branch('-a','--contains', commitSHA).split('\n')
      active = branch[0].split('* ')
      if len(active) > 1:
        if 'master' in active[1]:
          return [';name=' + name, commitSHA]
        else:
          return [';branch='+active[1] + ';name=' + name, commitSHA]
      else:
        non_active = branch[0].split('origin/')
        if len(non_active) > 1:
          return [';branch=' + non_active[1] + ';name=' + name, commitSHA]
        else:
          print('#ERROR: unknown response: ' + non_active[0])
          return ['', commitSHA]
    except:
      return [';nobranch=1;name=' + name, commitSHA]

  return ['', commitSHA]

def processData(key, repo, rev, root_folder):

  dest = key.split(':')
  destsuffix = re.sub('^%s' % 'src', root_folder, dest[0]) + '/'

  tmp1 = repo.replace('${platform}', 'linux-amd64')
  tmp2 = tmp1.replace('https://chrome-infra-packages.appspot.com/', 'cipd://')
  url = tmp2.replace('https://','git://')

  # get name
  tmp1 = url.replace('git://', '')
  tmp2 = tmp1.replace('cipd://', '')
  tmp3 = tmp2.replace('.', '_')
  tmp4 = tmp3.replace('-','_')
  name = "_".join(tmp4.split('/'))

  if 'git://' in url:
    lookup_url = url.replace('git://','https://')
    branch = get_branch(name, lookup_url, rev)
    return [destsuffix, url + ';protocol=https;destsuffix=' + destsuffix + branch[0] + ' \\', name, branch[1]]
  else:
    return [destsuffix, url + ';destsuffix=' + destsuffix + ';name=' + name + ' \\', name, rev]

def Var(obj, index, key):
  if key == 'host_os':
    return obj.host_os
  elif key == 'host_cpu':
    return obj.host_cpu
  elif key == 'platform':
    return obj.platform
  if 'custom_vars' in obj.solutions[index]:
    custom_vars = obj.solutions[index]['custom_vars']
    if key in custom_vars:
      return custom_vars[key]
  return obj.vars[key]

def Main(args):
  usage = ('usage: %prog [options]\n')
  parser = optparse.OptionParser(usage)
  parser.add_option('-o', '--output',
                    help='Specify the output file name. Defaults to: '
                         'gclient-entries.inc')
  parser.add_option('-r', '--root',
                    help='Specify the source root folder name to use in inc file. Defaults to: '
                         'src')

  (options, args) = parser.parse_args()

  if not options.output:
    options.output = 'gclient-entries.inc'
  if not options.root:
    options.root = 'src'

  print('output:  ' + options.output)
  print('root:    ' + options.root)

  # non-branch scenarios
  #branch = get_branch('chromium_googlesource_com_webm_libwebp', 'https://chromium.googlesource.com/webm/libwebp.git', '1.2.0')
  #print(branch)
  #branch = get_branch('flutter_googlesource_com_third_party_freetype2', 'https://flutter.googlesource.com/third_party/freetype2.git', '1f03c1b2d7f2ae832a4fbe9d12bd96c3c15bbece')
  #print(branch)

  freeze_support()

  # get .gclient solutions object
  shutil.copy2('.gclient', '_gclient.py')
  from _gclient import solutions

  # create file we can import
  with open('_deps.py', 'w') as f:
    with open('.gclient', 'r') as f1:
      gclient_contents = f1.read()
      f.write(gclient_contents)
      f.write('\n')
      f.write('platform = \'linux-amd64\'\n')
      f.write('host_os = \'linux\'\n')
      f.write('host_cpu = \'x64\'\n')
    with open('%s/%s' % (solutions[0]['name'], solutions[0]['deps_file']), 'r') as f1:
      deps_contents = f1.read()
      deps_contents = deps_contents.split('deps = {')
      f.write(deps_contents[0])
      f.write('def Var(key):\n')
      f.write('  if \'custom_vars\' in solutions[0]:\n')
      f.write('    custom_vars = solutions[0][\'custom_vars\']\n')
      f.write('    if key in custom_vars:\n')
      f.write('      print(\'using custom_vars\')\n')
      f.write('      return custom_vars[key]\n')
      f.write('  return vars[key]\n\n')
      f.write('deps = {\n')
      f.write(deps_contents[1])
    f.flush()


  os.remove('_gclient.py')

  import _deps

  # Root project entry
  root_path = _deps.solutions[0]['name']
  deps_file = _deps.solutions[0]['name'] + '/' + _deps.solutions[0]['deps_file']
  deps_file = re.sub('^%s' % 'src', options.root, deps_file)
  cwd = os.getcwd()
  os.chdir(_deps.solutions[0]['name'])
  root_commit = subprocess.check_output(['git', 'rev-parse', 'HEAD']).decode('ascii').strip()
  os.chdir(cwd)
  entries_obj = {}
  entries_obj[_deps.solutions[0]['name']] = {
    'url' : _deps.solutions[0]['url'],
    'rev' : root_commit
  }

  #
  # Enumerate Dependencies
  #
  for key, value in _deps.deps.items():
    if type(value) is not dict:
      val = value.split('@')
      entries_obj[key] = { 'url' : val[0], 'rev' : val[1] }
      #print('%s %s' % (key, value))
    else:
      if 'condition' in value:
        packages = value['packages']
        if '==' in value['condition']:
          tk = value['condition'].split(' ')
          tk[2] = tk[2].replace('\"','')
          if Var(_deps, 0, tk[0]) == tk[2]:
            for package in packages:
              name = 'cipd://' + package['package'].replace('${{platform}}',Var(_deps, 0,'platform'))
              entries_obj[key] = { 'url' : 'cipd://' + name, 'rev' : package['version'] }
              #print('cipd: %s' % (entries_obj[key]))
        else:
          if Var(_deps, 0, value['condition']) == True:
            for package in packages:
              name = 'cipd://' + package['package'].replace('${{platform}}',Var(_deps, 0,'platform'))
              entries_obj[key] = { 'url' : name, 'rev' : package['version'] }
              #print('cipd: %s' % (entries_obj[key]))
      else:
        if value['dep_type'] == 'cipd':
          packages = value['packages']
          for package in packages:
            name = 'cipd://' + package['package'].replace('${{platform}}',Var(_deps, 0,'platform'))
            entries_obj[key] = { 'url' : name, 'rev' : package['version'] }
            #print('cipd: %s' % (entries_obj[key]))


  # get hooks
  hook_dict = OrderedDict()
  for hook in _deps.hooks:
    if 'condition' in hook:
      if _deps.vars[hook['condition']] == True:
        hook_dict[hook['name']] = " ".join(hook['action'])
    else:
      hook_dict[hook['name']] = " ".join(hook['action'])


  task_queue = Queue()
  done_queue = Queue()
  src_dict = OrderedDict()

  for key in entries_obj:
    task_queue.put((processData,(key,entries_obj[key]['url'], entries_obj[key]['rev'], options.root)))

  NUMBER_OF_PROCESSES = cpu_count() - 1
  for i in range(NUMBER_OF_PROCESSES):
    Process(target=worker, args=(task_queue, done_queue)).start()

  for i in range(len(entries_obj)):
    res = done_queue.get()
    src_dict[res[0]] = [res[1], 'SRCREV_%s = \"%s\"' % (res[2], res[3])]

  for i in range(NUMBER_OF_PROCESSES):
    task_queue.put('STOP')

  #
  # write output file
  #

  sorted_indices=sorted(src_dict.keys(), key=lambda x:x.lower())

  with open(options.output, 'w') as f:

    f.write('##############################################\n')
    f.write('#### Auto generated by gclient_bitbake.py ####\n')
    f.write('##############################################\n\n')

    f.write('DEPS_FILE = \"%s\"\n' % (deps_file))
    if _deps.gclient_gn_args_file:
      f.write('GCLIENT_GN_ARGS_FILE = \"%s\"\n' % (_deps.gclient_gn_args_file))
    if _deps.gclient_gn_args:
      f.write('GCLIENT_GN_ARGS = \"%s\"\n' % (' '.join(_deps.gclient_gn_args)))
    f.write('\n')

    for i in sorted_indices:
      if src_dict[i][0].startswith('cipd://'):
        f.write('inherit cipd\n\n')
        break

    f.write('SRC_URI += \"\\\n')

    for i in sorted_indices:
      f.write('    %s\n' % (src_dict[i][0]))

    f.write(' "\n\n')

    for i in sorted_indices:
      f.write('%s\n' % (src_dict[i][1]))

    f.write('\n\n')

    #
    # run hooks
    #
    f.write('addtask run_hooks before do_patch after do_unpack\n')
    f.write('do_run_hooks[dirs] = \"${S}\"\n')
    f.write('do_run_hooks[depends] += \" \\\n')
    f.write('    ca-certificates-native:do_populate_sysroot \\\n')
    f.write('    curl-native:do_populate_sysroot \\\n')
    f.write('\"\n\n')

    f.write('python do_run_hooks() {\n\n')

    f.write('    import os\n')
    f.write('    import bb\n')
    f.write('    import errno\n')
    f.write('    from   bb.fetch2 import runfetchcmd\n\n')

    f.write('    workdir = d.getVar("WORKDIR")\n')
    f.write('    curl_ca_bundle = d.getVar("CURL_CA_BUNDLE")\n\n')

    if _deps.gclient_gn_args_file:
      f.write('    # create gclient_gn_args_file\n')
      f.write('    gclient_gn_args_filepath = os.path.dirname(\'%s\')\n' % (_deps.gclient_gn_args_file))
      f.write('    os.makedirs(gclient_gn_args_filepath, exist_ok=True)\n')
      f.write('    runfetchcmd(\'echo \\\'# Generated by bitbake - do_run_hooks()\\\' > %s\', d, quiet=False, workdir=workdir)\n' % (_deps.gclient_gn_args_file))

    for key, value in hook_dict.items():
      f.write('\n    # %s\n' % (key))
      f.write("    runfetchcmd(\'export CURL_CA_BUNDLE=%%s; %s\' %% (curl_ca_bundle), d, quiet=False, workdir=workdir)\n" % (value))
    f.write('}\n')

  os.remove('_deps.py')

if __name__ == '__main__':
  sys.exit(Main(sys.argv))
