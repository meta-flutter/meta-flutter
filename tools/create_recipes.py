#!/usr/bin/env python3
# SPDX-FileCopyrightText: (C) 2020-2024 Joel Winarske
# SPDX-License-Identifier: Apache-2.0
#
# Script to create Yocto recipes from a given path.
#

import os
import signal
import sys

from common import get_yaml_obj
from common import make_sure_path_exists
from common import print_banner


def get_file_md5(file_name):
    import hashlib
    with open(file_name, 'rb') as f:
        data = f.read()
        md5_returned = hashlib.md5(data).hexdigest()
        return md5_returned


def main():
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument('--path', default='', type=str, help='Path to enumerate pubspec.yaml')
    parser.add_argument('--license', default='', type=str, help='License file relative to path value')
    parser.add_argument('--license_type', default='CLOSED', type=str, help='License type')
    parser.add_argument('--author', default='', type=str, help='value to assign to AUTHOR')
    parser.add_argument('--patch-dir', default='', type=str, help='Path to patch folder')
    parser.add_argument('--out', default='.', type=str, help='Output path to create the Yocto recipes in')
    args = parser.parse_args()

    if args.path == '':
        sys.exit("Must specify value for --path")

    if args.out == '':
        sys.exit("Must specify value for --out")

    #
    # Control+C handler
    #
    from common import handle_ctrl_c
    signal.signal(signal.SIGINT, handle_ctrl_c)

    #
    # Create yocto recipes from folder
    #
    if args.path:
        if not os.path.isdir(args.path):
            raise Exception(f'--path {args.path} is not a directory')

        # Check license file if specified
        license_md5 = ''
        if args.license:
            license_path = args.path + '/' + args.license
            if not os.path.isfile(license_path):
                raise Exception('--license {license_path} is not present')

            if args.license_type != 'CLOSED':
                license_md5 = get_file_md5(license_path)

        create_yocto_recipes(directory=args.path,
                             license_file=args.license,
                             license_type=args.license_type,
                             license_md5=license_md5,
                             author=args.author,
                             recipe_folder=None,
                             output_path=args.out,
                             package_output_path=args.out,
                             ignore_list=[],
                             rdepends_list=[],
                             output_path_override_list=[],
                             src_folder="",
                             src_files=[],
                             entry_files=[],
                             variables=[],
                             patch_dir=args.patch_dir,
                             compiler_requires_network_list=[])
        return


def get_process_stdout(cmd, directory):
    import subprocess

    process = subprocess.Popen(
        cmd, shell=True, stdout=subprocess.PIPE, universal_newlines=True, cwd=directory)
    ret = ""
    for line in process.stdout:
        ret += str(line)
    process.wait()
    return ret.strip()


def get_git_branch(directory: str, commit: str) -> str:
    """Get branch name"""
    response = get_process_stdout(f'git branch --contains {commit}', directory)
    if '(HEAD detached' in response:
        return ''
    else:
        return response.split(' ')[-1]


def get_git_commit_hash_for_tag(directory: str, tag: str) -> str:
    """Get commit hash for tag name"""
    response = get_process_stdout(f'git rev-list -n 1 tags/{tag}', directory)
    if 'fatal: ambiguous argument' in response:
        return ''
    else:
        return response.split(' ')[-1]


def get_repo_vars(directory):
    """Gets variables associated with repository"""

    if not os.path.isdir(directory):
        print_banner(f'ERROR: {directory} is not valid')
        raise Exception(f'{directory} is not valid')

    git_path = directory + '.git'
    if not os.path.isdir(git_path):
        print_banner(f'ERROR: {directory} is not a git repository')
        raise Exception(f'{directory} is not a git repository')

    remote_verbose = get_process_stdout('git remote -v', directory)
    remote_verbose = remote_verbose.split(' ')[0]
    remote_lines = remote_verbose.split('\n')
    remote_tokens = remote_lines[0]
    remote_lines = remote_tokens.split('\t')
    remote_lines[1] = remote_lines[1].replace('git@', 'https')
    repo = remote_lines[1].rsplit(sep='/', maxsplit=2)

    org = repo[-2].lower()
    repo = repo[-1].lower()
    unit = repo.split('.')

    submodules = False
    if os.path.isfile(directory + '/.gitmodules'):
        submodules = True

    lfs = False
    if os.path.isfile(directory + '/.gitattributes'):
        lfs = True

    commit = get_process_stdout('git rev-parse --verify HEAD', directory)

    branch = get_git_branch(directory, commit)

    url_raw = get_process_stdout('git config --get remote.origin.url', directory)
    url = url_raw.split('//')

    value = ''
    if len(url) > 1:
        value = url[1]
    else:
        url = url_raw.split('@')
        if len(url) > 1:
            value = url[1]
        else:
            print('delimiter not handled')

    return org, unit[0], submodules, value, lfs, branch, commit


def dedupe_adjacent(iterable):
    prev = object()
    for item in iterable:
        if item != prev:
            prev = item
            yield item


def get_recipe_name(org, unit, flutter_application_path, project_name) -> str:
    """Gets recipe name string"""

    # check if org and unit have overlap
    org_tokens = org.split('-')
    unit_tokens = unit.split('-')
    if org_tokens[-1] == unit_tokens[0]:
        tmp_header = org_tokens + unit_tokens[-1:]
        header = '-'.join(tmp_header)
    else:
        header = f'{org}-{unit}'

    if project_name:
        project_name = project_name.replace('_', '-')

    app_path = flutter_application_path.replace('/', '-')
    app_path = app_path.replace('_', '-')

    app_path_tokens = app_path.split('-')
    header_tokens = header.split('-')

    if header_tokens[-1] == app_path_tokens[0]:
        tmp_app_path = header_tokens + app_path_tokens[:-1]
        app_path = '-'.join(tmp_app_path)

    if app_path.startswith(header):
        if project_name and project_name not in app_path:
            recipe_name = f'{app_path}-{project_name}'
        else:
            recipe_name = app_path
    else:
        if project_name and project_name not in app_path:
            recipe_name = f'{header}-{app_path}-{project_name}'
        else:
            recipe_name = f'{header}-{app_path}'

    recipe_name = recipe_name.replace('_', '-')
    recipe_name = recipe_name.replace('--', '-')

    vals = dedupe_adjacent(recipe_name.split('-'))
    recipe_name = '-'.join(vals)
    if recipe_name.endswith('-'):
        recipe_name = recipe_name[:-1]

    return recipe_name.lower()


def copy_src_file(file: str, src_folder: str, patch_dir: str, output_path: str):
    print(f'copy_src_file: {file}, {src_folder}, {patch_dir}, {output_path}')
    import shutil

    file = file.split(';')[0]
    src_file = os.path.join(patch_dir, src_folder, file)
    if not os.path.exists(src_file):
        print(f'Missing: {src_file}')
        return

    dst_folder = os.path.join(output_path, 'files', src_folder)
    dst_file = os.path.join(dst_folder, file)

    make_sure_path_exists(dst_folder)
    shutil.copy2(src_file, dst_file)


def create_recipe(directory,
                  pubspec_yaml,
                  flutter_application_path,
                  org, unit, submodules, url, lfs, branch, commit,
                  license_file, license_type, license_md5,
                  author,
                  recipe_folder,
                  output_path,
                  rdepends_list,
                  output_path_override_list,
                  compiler_requires_network_list,
                  src_folder,
                  src_files,
                  variables,
                  patch_dir) -> str:
    is_web = False
    # TODO detect web

    # pubspec.yaml key/values
    yaml_obj = get_yaml_obj(pubspec_yaml)
    if len(yaml_obj) == 0:
        print(f'Invalid YAML: {pubspec_yaml}')
        return ''

    project_name = yaml_obj.get('name')
    project_description = yaml_obj.get('description')
    project_homepage = yaml_obj.get('repository')
    project_issue_tracker = yaml_obj.get('issue_tracker')
    project_version = yaml_obj.get('version')

    if output_path_override_list and flutter_application_path in output_path_override_list:
        output_path = os.path.join(output_path, output_path_override_list[flutter_application_path])
        if recipe_folder:
            output_path = os.path.join(str(output_path), recipe_folder)
        print(f'Output Path: {output_path}')
    elif recipe_folder:
        output_path = os.path.join(output_path, 'recipes-graphics', 'flutter-apps', recipe_folder)
    else:
        output_path = os.path.join(output_path, 'recipes-graphics', 'flutter-apps')

    make_sure_path_exists(str(output_path))

    recipe_name = get_recipe_name(org, unit, flutter_application_path, project_name)

    if project_version is not None:
        version = project_version.split('+')
        filename = f'{output_path}/{recipe_name}_{version[0]}.bb'
    else:
        filename = f'{output_path}/{recipe_name}_git.bb'

    print(f'pubspec_yaml: {pubspec_yaml}')
    print(f'filename: {filename}')
    with open(filename, "w") as f:
        f.write('#\n')
        f.write('# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.\n')
        f.write('#\n')
        f.write('\n')

        if project_name:
            project_name = project_name.strip()
        if project_description:
            project_description = project_description.strip()
        if author:
            author = author.strip()
        if project_homepage:
            project_homepage = project_homepage.strip()
        if project_issue_tracker:
            project_issue_tracker = project_issue_tracker.strip()

        f.write(f'SUMMARY = "{project_name}"\n')
        f.write(f'DESCRIPTION = "{project_description}"\n')
        f.write(f'AUTHOR = "{author}"\n')
        f.write(f'HOMEPAGE = "{project_homepage}"\n')
        f.write(f'BUGTRACKER = "{project_issue_tracker}"\n')

        f.write('SECTION = "graphics"\n')
        f.write('\n')

        f.write(f'LICENSE = "{license_type}"\n')
        if license_type != 'CLOSED':
            f.write(f'LIC_FILES_CHKSUM = "file://{license_file};md5={license_md5}"\n')

        f.write('\n')
        f.write(f'SRCREV = "{commit}"\n')

        if submodules:
            fetcher = 'gitsm'
        else:
            fetcher = 'git'
        if lfs:
            lfs_option = 'lfs=1'
        else:
            lfs_option = 'lfs=0'
        if branch:
            branch_option = f'branch={branch}'
        else:
            branch_option = f'nobranch=1'

        if patch_dir and src_files:
            f.write('SRC_URI = " \\\n')
            f.write(f'    {fetcher}://{url};{lfs_option};{branch_option};protocol=https;destsuffix=git \\\n')
            files = src_files.get(f'{flutter_application_path}')
            if files:
                for file in files:
                    if src_folder:
                        copy_src_file(file, src_folder, patch_dir, str(output_path))
                        f.write(f'    file://{src_folder}/{file} \\\n')
                    else:
                        f.write(f'    file://{file} \\\n')
                f.write('\"\n')
        else:
            f.write(f'SRC_URI = "{fetcher}://{url};{lfs_option};{branch_option};protocol=https;destsuffix=git"\n')
        f.write('\n')
        f.write('S = "${WORKDIR}/git"\n')
        f.write('\n')

        # detect melos
        if os.path.isfile(directory + '/melos.yaml'):
            f.write('PUB_CACHE_EXTRA_ARCHIVE_PATH = "${WORKDIR}/pub_cache/bin"\n')
            f.write('PUB_CACHE_EXTRA_ARCHIVE_CMD = "flutter pub global activate melos; \\\n')
            f.write('    melos bootstrap"\n')
            f.write('\n')

        f.write(f'PUBSPEC_APPNAME = "{project_name}"\n')
        f.write(f'FLUTTER_APPLICATION_INSTALL_SUFFIX = "{recipe_name}"\n')
        f.write(f'PUBSPEC_IGNORE_LOCKFILE = "1"\n')

        f.write(f'FLUTTER_APPLICATION_PATH = "{flutter_application_path}"\n')

        # variables
        if variables:
            values = variables.get(f'{flutter_application_path}')
            for var in values:
                f.write(f'{var}\n')
        f.write('\n')

        if is_web:
            f.write('inherit flutter-web\n')
        else:
            f.write('inherit flutter-app\n')

        if rdepends_list and flutter_application_path in rdepends_list:
            rdepends = rdepends_list[flutter_application_path]
            f.write('\n')
            f.write('RDEPENDS:${PN} += " \\\n')
            for rdepend in rdepends:
                f.write(f'    {rdepend} \\\n')
            f.write(f'"\n')

        if compiler_requires_network_list and flutter_application_path in compiler_requires_network_list:
            f.write('\n')
            f.write('do_compile[network] = "1"\n')
            f.write('\n')

        return recipe_name


def create_package_group(org, unit, recipes,
                         output_path_override_list,
                         output_path):
    """Create package group file"""
    print_banner("Creating Package Group recipe")

    recipe_name = get_recipe_name(org, unit, '', None)
    filename = f'{output_path}/packagegroup-{recipe_name}.bb'
    filename = filename.replace('_', '-')

    with open(filename, "w") as f:
        f.write('#\n')
        f.write('# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.\n')
        f.write('#\n')
        f.write('\n')
        f.write(f'SUMMARY = "Package of Flutter {org} {unit} apps"\n')
        f.write('\n')
        f.write('PACKAGE_ARCH = "${MACHINE_ARCH}"\n')
        f.write('\n')
        f.write('inherit packagegroup\n')
        f.write('\n')
        f.write('RDEPENDS:${PN} += " \\\n')
        for item in recipes:
            recipe = item[0]

            # don't include custom path based recipes in package recipe
            if output_path_override_list:
                flutter_app_path = item[1]
                if flutter_app_path in output_path_override_list:
                    continue
            f.write(f'    {recipe} \\\n')
        f.write('"\n')


def create_yocto_recipes(directory,
                         license_file, license_type, license_md5,
                         author,
                         recipe_folder,
                         output_path,
                         package_output_path,
                         ignore_list,
                         rdepends_list,
                         output_path_override_list,
                         compiler_requires_network_list,
                         src_folder,
                         src_files,
                         entry_files,
                         variables,
                         patch_dir):
    """Create bb recipe for each pubspec.yaml file in path"""
    import glob

    print_banner("Creating Yocto Recipes")

    if not directory.endswith('/'):
        directory += '/'

    print(f'ignore_list: {ignore_list}')
    print(f'rdepends_list: {rdepends_list}')
    print(f'output_path_override_list: {output_path_override_list}')
    print(f'compiler_requires_network_list: {compiler_requires_network_list}')
    print(f'src_folder: {src_folder}')
    print(f'src_files: {src_files}')
    print(f'entry_files: {entry_files}')
    print(f'variables: {variables}')
    print(f'patch_dir: {patch_dir}')

    #
    # Get repo variables
    #
    org, unit, submodules, url, lfs, branch, commit = get_repo_vars(directory)

    #
    # Iterate all pubspec.yaml files
    #
    recipes = []
    for filename in glob.iglob(directory + '**/pubspec.yaml', recursive=True):

        # handle invalid pubspec.yaml files
        yaml_obj = get_yaml_obj(filename)
        if len(yaml_obj) == 0:
            print(f'Invalid YAML: {filename}')
            continue

        path_tokens = filename.split('/')

        if path_tokens[-1] != 'pubspec.yaml':
            print_banner(f'ERROR: invalid {filename}')
            return ''

        # get relative path
        directory_tokens = directory.split('/')
        # remove end null entry
        del directory_tokens[-1]

        # copy list
        app_path = path_tokens
        for i in range(len(directory_tokens)):
            del app_path[0]
        flutter_application_path = '/'.join(app_path[:-1])

        entry_file = 'main.dart'
        if entry_files:
            if flutter_application_path in entry_files:
                entry_file = entry_files.get(f'{flutter_application_path}')[0]

        lib_main_dart = os.path.join(directory, flutter_application_path, 'lib', entry_file)
        if not os.path.exists(lib_main_dart):
            continue

        # filtering
        if ignore_list and flutter_application_path in ignore_list:
            print(f'Ignoring: {flutter_application_path}')
            continue

        recipe = create_recipe(directory=directory,
                               pubspec_yaml=filename,
                               flutter_application_path=flutter_application_path,
                               org=org, unit=unit, submodules=submodules, url=url, lfs=lfs, branch=branch,
                               commit=commit,
                               license_file=license_file, license_type=license_type, license_md5=license_md5,
                               author=author,
                               recipe_folder=recipe_folder,
                               output_path=output_path,
                               rdepends_list=rdepends_list,
                               output_path_override_list=output_path_override_list,
                               compiler_requires_network_list=compiler_requires_network_list,
                               src_folder=src_folder,
                               src_files=src_files,
                               variables=variables,
                               patch_dir=patch_dir)

        if recipe != '':
            recipes.append([recipe, flutter_application_path])

    create_package_group(org, unit, recipes,
                         output_path_override_list,
                         package_output_path)

    print_banner("Creating Yocto Recipes done.")


if __name__ == "__main__":
    from common import check_python_version

    check_python_version()

    main()
