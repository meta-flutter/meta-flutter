#!/usr/bin/env python3
# SPDX-FileCopyrightText: (C) 2020-2024 Joel Winarske
# SPDX-License-Identifier: MIT
#
# Script to roll meta-flutter layer
#

import json
import os
import shutil
import signal
import subprocess
import sys

from common import detect_licenses
from common import license_agrees_with_source
from common import LICENSE_OPERATOR
from common import wrong_license_operator
from common import make_sure_path_exists
from common import print_banner


class RollError(Exception):
    """A repo could not be rolled.

    Raised rather than exit()ed: get_repo() runs in a worker thread, where
    SystemExit ends that thread and nothing else. It reads like it stops the
    script and does not.
    """


def get_flutter_apps(filename) -> dict:
    filepath = os.path.join(filename)
    with open(filepath, 'r') as f:
        try:
            return json.load(f)
        except json.decoder.JSONDecodeError:
            print("Invalid JSON in %s" % f)
            exit(1)


def clear_folder(dir_):
    """ Clears folder specified """
    if os.path.exists(dir_):
        shutil.rmtree(dir_)


def get_repo(repo_path: str, output_path: str,
             uri: str, branch: str, rev: str, license_file: str, license_type: str,
             validate_license: bool,
             author: str,
             recipe_folder: str,
             package_output_path: str,
             ignore_list: dict,
             rdepends_list: dict,
             output_path_override_list: dict,
             compiler_requires_network_list: dict,
             src_folder: str,
             src_files: dict,
             entry_files: dict,
             variables: dict,
             patch_dir: str,
             pubvendor: bool = False):
    """ Clone Git Repo """

    print(f'repo_path: {repo_path}')
    print(f'output_path: {output_path}')
    print(f'recipe_folder: {recipe_folder}')
    print(f'package_output_path: {package_output_path}')

    # if not ignore_list:
    #    ignore_list = []
    # if not rdepends_list:
    #    rdepends_list = []
    # if not output_path_override_list:
    #    output_path_override_list = []

    # Not "skip": a manifest entry that cannot be cloned is a manifest bug,
    # and skipping it produced a roll that quietly generated nothing for that
    # repo. The flutter/flutter entry sat branchless in flutter-apps.json
    # doing exactly that.
    if not uri:
        raise RollError("repo entry has no 'uri'")
    if not branch:
        raise RollError(f"{uri}: repo entry has no 'branch'")

    # get repo folder name
    from urllib.parse import urlparse
    url_parse_res = urlparse(uri)
    path = url_parse_res.path.split('.', 1)[0]
    repo_name = path.rsplit('/', 1)[-1]

    git_folder: str = os.path.join(repo_path, repo_name)

    git_folder_git: str = os.path.join(repo_path, repo_name, '.git')

    is_exist = os.path.exists(git_folder_git)
    if is_exist:

        cmd = ['git', 'reset', '--hard']
        subprocess.check_call(cmd, cwd=git_folder)

        cmd = ['git', 'fetch', '--all']
        subprocess.check_call(cmd, cwd=git_folder)

        cmd = ['git', 'checkout', branch]
        subprocess.check_call(cmd, cwd=git_folder)

    else:

        cmd = ['git', 'clone', uri, '-b', branch, repo_name]
        subprocess.check_call(cmd, cwd=repo_path)

    if rev:
        cmd = ['git', 'reset', '--hard', rev]
        subprocess.check_call(cmd, cwd=git_folder)

    # get lfs
    git_lfs_file = os.path.join(git_folder, '.gitattributes')
    if os.path.exists(git_lfs_file):
        cmd = ['git', 'lfs', 'fetch', '--all']
        subprocess.check_call(cmd, cwd=git_folder)

    # get all submodules
    git_submodule_file = os.path.join(git_folder, '.gitmodules')
    if os.path.exists(git_submodule_file):
        cmd = ['git', 'submodule', 'update', '--init', '--recursive']
        subprocess.check_call(cmd, cwd=git_folder)

    license_md5 = ''
    if license_file:
        license_path = os.path.join(repo_path, repo_name, license_file)
        if not os.path.isfile(license_path):
            print_banner(f'ERROR: {license_path} is not present')
            raise RollError(f'{repo_name}: {license_file} is not present')

        # Check the declared license against the text the recipe will ship.
        # flutter-apps.json states a license for someone else's repository and
        # nothing re-checked it once written, which is how depot-tools carried
        # LICENSE = "GPLv3" over BSD-3-Clause text. Upstreams also relicense and
        # add licenses between rolls -- flutter/games grew an Apache-2.0 section
        # and a bundled font's OFL-1.1 -- and a roll is when that surfaces.
        # The operator is branch-specific and each spelling is a regression on
        # the other branch, so a manifest entry carrying the wrong one has to
        # fail here -- it generates recipes in the wrong dialect for the
        # oe-core this branch builds against, and nothing downstream looks.
        # A cherry-picked entry is the likely way in.
        wrong = wrong_license_operator(license_type)
        if wrong:
            print_banner(
                f'ERROR: {repo_name}: license_type joins licenses with '
                f'"{wrong}", but this branch uses "{LICENSE_OPERATOR}"\n'
                f'  declared: {license_type}\n'
                f'Fix license_type in flutter-apps.json. See the LICENSE '
                f'operator note in README.')
            raise RollError(
                f'{repo_name}: license_type joins licenses with "{wrong}", '
                f'but this branch uses "{LICENSE_OPERATOR}"')

        if validate_license:
            detected = detect_licenses(license_path)
            if not detected:
                print(f'NOTE: {repo_name}: license text not recognized, '
                      f'leaving "{license_type}" unchecked')
            elif not license_agrees_with_source(license_type, detected):
                print_banner(
                    f'ERROR: {repo_name}: declared license does not match its source\n'
                    f'  declared: {license_type}\n'
                    f'  {license_file} contains: {" AND ".join(detected)}\n'
                    f'Fix license_type in flutter-apps.json, or set '
                    f'"license_validate": false for a license this check reads wrong.')
                raise RollError(
                    f'{repo_name}: declared license {license_type} does not '
                    f'match {license_file}')

        if license_type != 'CLOSED':
            from create_recipes import get_file_md5
            license_md5 = get_file_md5(license_path)

    elif license_type != 'CLOSED':
        # A license type with no file to back it cannot produce LIC_FILES_CHKSUM.
        print_banner(f'ERROR: {repo_name}: license_type "{license_type}" declared '
                     f'without a license_file')
        raise RollError(f'{repo_name}: license_type "{license_type}" declared '
                        f'without a license_file')

    repo_path = os.path.join(repo_path, repo_name)

    from create_recipes import create_yocto_recipes
    create_yocto_recipes(directory=repo_path,
                         license_file=license_file,
                         license_type=license_type,
                         license_md5=license_md5,
                         author=author,
                         recipe_folder=recipe_folder,
                         output_path=output_path,
                         package_output_path=package_output_path,
                         ignore_list=ignore_list,
                         rdepends_list=rdepends_list,
                         output_path_override_list=output_path_override_list,
                         compiler_requires_network_list=compiler_requires_network_list,
                         src_folder=src_folder,
                         src_files=src_files,
                         entry_files=entry_files,
                         variables=variables,
                         patch_dir=patch_dir,
                         pubvendor=pubvendor)


def get_workspace_repos(repo_path, repos, output_path, package_output_path, patch_dir):
    """ Clone GIT repos referenced in config repos dict to base_folder """

    import concurrent.futures

    futures = {}
    with concurrent.futures.ThreadPoolExecutor() as executor:
        for r in repos:
            futures[executor.submit(get_repo, repo_path=repo_path,
                                    output_path=output_path,
                                    package_output_path=package_output_path,
                                    uri=r.get('uri'),
                                    branch=r.get('branch'),
                                    rev=r.get('rev'),
                                    license_file=r.get('license_file'),
                                    license_type=r.get('license_type'),
                                    validate_license=r.get('license_validate', True),
                                    author=r.get('author'),
                                    recipe_folder=r.get('folder'),
                                    ignore_list=r.get('ignore'),
                                    rdepends_list=r.get('rdepends'),
                                    output_path_override_list=r.get('output_folder'),
                                    compiler_requires_network_list=r.get('compiler_requires_network'),
                                    pubvendor=r.get('pubvendor', False),
                                    src_folder=r.get('src_folder'),
                                    src_files=r.get('src_files'),
                                    entry_files=r.get('entry_files'),
                                    variables=r.get('variables'),
                                    patch_dir=patch_dir)] = r.get('uri') or '<no uri>'

    # Read every future. Submitting and never calling result() means an
    # exception in a worker is captured in its Future and re-raised nowhere:
    # a failed clone, a license mismatch, anything create_yocto_recipes()
    # raises. The roll then exits 0 having generated nothing for that repo,
    # which run by hand scrolls past and run from CI is a green build with
    # recipes silently missing.
    failed = []
    for future, uri in futures.items():
        try:
            future.result()
        except BaseException as e:
            failed.append((uri, e))

    if failed:
        print_banner(f'{len(failed)} of {len(futures)} repos failed to roll')
        for uri, e in failed:
            print(f'  {uri}\n      {type(e).__name__}: {e}')
        sys.exit(1)

    print_banner("Repos Cloned")


def update_flutter_version_inc(include_path, flutter_sdk_version):
    flutter_version_inc = os.path.join(include_path, 'flutter-version.inc')

    print(f'Updating {flutter_version_inc} to {flutter_sdk_version}')

    with open(flutter_version_inc, 'r') as f:
        lines = f.readlines()

    with open(flutter_version_inc, 'w') as f:
        for line in lines:
            if 'FLUTTER_SDK_TAG ??=' in line:
                line = f'FLUTTER_SDK_TAG ??= "{flutter_sdk_version}"\n'
            f.write(line)


def get_dart_sdk_version(root_path: str, flutter_sdk_version: str) -> str:
    import re
    dart_revision = os.path.join(root_path, 'conf', 'include', 'dart-revision.json')
    with open(dart_revision, 'r') as f:
        dart_sdk_version = json.load(f).get(flutter_sdk_version, None)
        if ' (build ' in dart_sdk_version:
            dart_sdk_version = re.sub(r'^.*?build ', '', dart_sdk_version)
        if ')' in dart_sdk_version:
            return dart_sdk_version[:-1]
        return dart_sdk_version


def get_current_release(root_path: str) -> dict:
    release_linux = os.path.join(root_path, 'conf', 'include', 'releases_linux.json')
    with open(release_linux, 'r') as f:
        return json.load(f).get('current_release', dict())


def get_release(root_path: str, commit_hash: str) -> dict:
    release_linux = os.path.join(root_path, 'conf', 'include', 'releases_linux.json')
    with open(release_linux, 'r') as f:
        for release in json.load(f).get('releases', []):
            if 'hash' in release and commit_hash == release['hash']:
                return release


def update_dart_recipe(root_path: str, flutter_sdk_version: str):
    import glob

    from create_recipes import get_git_commit_hash_for_tag

    dart_sdk_version = get_dart_sdk_version(root_path, flutter_sdk_version)
    if not dart_sdk_version:
        print_banner("Dart SDK version is not available")
        return

    new_dart_recipe_path = os.path.join(root_path, 'recipes-devtools', 'dart', f'dart-sdk_{dart_sdk_version}.bb')

    dart_recipe_path_root = os.path.join(root_path, 'recipes-devtools', 'dart')
    dart_recipe_path = glob.glob(f'{dart_recipe_path_root}/dart-sdk*.bb')

    if new_dart_recipe_path == dart_recipe_path[0]:
        print_banner("Skipping Dart SDK update: version already exists")
        return

    print(f'Updating dart-sdk recipe to {dart_sdk_version}')

    # get tag commit hash

    tmp_path = os.path.join(root_path, '.dart')
    if os.path.exists(tmp_path):
        clear_folder(tmp_path)
    cmd = ['git', 'clone', 'https://github.com/dart-lang/sdk', '.dart']
    subprocess.check_call(cmd, cwd=root_path)
    commit_hash = get_git_commit_hash_for_tag(tmp_path, dart_sdk_version)

    # create new recipe updating SRCREV

    with open(dart_recipe_path[0], 'r') as fi, open(new_dart_recipe_path, 'w') as fo:
        for line in fi:
            if 'SRCREV =' in line:
                line = f'SRCREV = "{commit_hash}"\n'
            fo.write(line)

    os.remove(dart_recipe_path[0])

    report_removed_gn_options(root_path, new_dart_recipe_path, tmp_path, commit_hash)

    clear_folder(tmp_path)


def report_removed_gn_options(root_path: str, recipe_path: str, sdk_clone: str, commit_hash: str):
    """Warn about PACKAGECONFIG options the new Dart SDK's tools/gn.py dropped.

    The recipe is copied forward verbatim apart from SRCREV, so PACKAGECONFIG
    entries survive a version bump even when the option they pass no longer
    exists. gn.py uses argparse, which exits 2 on an unrecognised option, so the
    result is a do_configure failure well after the roll, with nothing pointing
    back at the cause.
    """
    import re

    try:
        gn_py = subprocess.check_output(
            ['git', 'show', f'{commit_hash}:tools/gn.py'],
            cwd=sdk_clone, text=True, stderr=subprocess.DEVNULL)
    except (subprocess.CalledProcessError, FileNotFoundError):
        print('Could not read tools/gn.py from the Dart SDK; '
              'skipping PACKAGECONFIG option check')
        return

    # Every long option mentioned anywhere in gn.py, including help text. This
    # deliberately over-approximates: it is only ever used to decide that an
    # option is *absent*, so a loose match means fewer false alarms.
    known = set(re.findall(r'--[a-z0-9][a-z0-9-]*', gn_py))

    removed = []
    with open(recipe_path, 'r') as f:
        for line in f:
            m = re.match(r'\s*PACKAGECONFIG\[([^\]]+)\]\s*=\s*(.+)', line)
            if not m:
                continue
            name, value = m.group(1), m.group(2)
            for opt in re.findall(r'--[a-z0-9][a-z0-9-]*', value):
                if opt not in known:
                    removed.append((name, opt))

    if not removed:
        return

    print_banner('PACKAGECONFIG options no longer accepted by tools/gn.py')
    for name, opt in removed:
        print(f'  PACKAGECONFIG[{name}] passes {opt}, which this Dart SDK '
              f'does not accept')
    print('')
    print(f'Remove these from {os.path.relpath(recipe_path, root_path)} before '
          f'building; gn.py exits 2 on an unrecognised option, so do_configure '
          f'will fail otherwise.')


def main():
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument('--channel', default='stable', type=str, help='Flutter Channel - beta, dev, stable')
    parser.add_argument('--version', default=None, type=str, help='Flutter SDK version')
    parser.add_argument('--path', default='.', type=str, help='meta-flutter root path')
    parser.add_argument('--json', default=None, type=str,
                        help='JSON file of flutter apps '
                             '(default: <path>/meta-flutter-apps/conf/flutter-apps.json)')
    parser.add_argument('--patch-dir', default=None, type=str,
                        help='Path to patch folder (default: <path>/tools/patches)')
    args = parser.parse_args()

    #
    # These defaults describe files inside the meta-flutter tree, so resolve
    # them against --path. As literal './...' defaults they were relative to the
    # working directory instead, and running the script from anywhere other than
    # the layer root silently looked for them in the wrong place.
    #
    if args.json is None:
        args.json = os.path.join(args.path, 'meta-flutter-apps', 'conf', 'flutter-apps.json')
    if args.patch_dir is None:
        args.patch_dir = os.path.join(args.path, 'tools', 'patches')

    #
    # Control+C handler
    #
    from common import handle_ctrl_c
    signal.signal(signal.SIGINT, handle_ctrl_c)

    if not os.path.exists(args.path):
        make_sure_path_exists(args.path)

    include_path = os.path.join(args.path, 'conf', 'include')

    print_banner(f'Rolling meta-flutter')

    #
    # Refresh the release data before choosing a version. get_current_release()
    # and get_release() read conf/include/releases_linux.json, so running this
    # afterwards -- as it used to -- selected the channel version from whatever
    # was last committed to the repo rather than what the channel currently
    # points at, and the roll silently targeted a stale release.
    # get_version_files() downloads releases_linux.json itself and does not
    # depend on the selected version, so it is safe to run first.
    #
    print_banner('Updating version files')
    from update_version_files import get_version_files
    get_version_files(include_path)
    print_banner('Done updating version files')

    #
    # if version is not specified use channel version
    #

    if args.version is not None:
        flutter_sdk_version = args.version
    else:
        current_release = get_current_release(args.path)
        if args.channel not in current_release:
            sys.exit(f'Channel [{args.channel}] not found in current release')

        release = get_release(args.path, current_release[args.channel])
        if 'version' not in release:
            sys.exit(f'Unable to determine channel version for {args.channel}')

        flutter_sdk_version = release['version']
        if not flutter_sdk_version:
            sys.exit(f'Flutter SDK version not found in release for channel {args.channel}')

    update_flutter_version_inc(include_path, flutter_sdk_version)

    print_banner(f'Updating dart-sdk recipe')
    update_dart_recipe(args.path, flutter_sdk_version)

    print_banner(f'Updating meta-flutter-apps from {args.json}')
    repos = get_flutter_apps(args.json)

    # scratch dir belongs beside the tree being rolled, matching .dart in
    # update_dart_recipe(), rather than wherever the script happened to be run
    repo_path = os.path.join(args.path, '.flutter-apps')
    clear_folder(repo_path)
    make_sure_path_exists(repo_path)

    package_output_path = os.path.join(args.path, 'meta-flutter-apps', 'recipes-platform', 'packagegroups')
    make_sure_path_exists(package_output_path)

    output_path = os.path.join(args.path, 'meta-flutter-apps')
    make_sure_path_exists(output_path)

    get_workspace_repos(repo_path, repos, output_path, package_output_path, args.patch_dir)

    clear_folder(repo_path)

    print_banner('Done')


if __name__ == "__main__":
    from common import check_python_version
    from common import test_internet_connection

    check_python_version()

    if not test_internet_connection():
        sys.exit("roll_meta_flutter.py requires an internet connection")

    main()
