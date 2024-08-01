#!/usr/bin/env python3
#
# SPDX-FileCopyrightText: (C) 2020-2023 Joel Winarske
#
# SPDX-License-Identifier: Apache-2.0
#
#
# Script to roll meta-flutter layer

import os
import signal
import subprocess
import sys

from create_recipes import create_yocto_recipes
from create_recipes import get_file_md5
from fw_common import check_python_version
from fw_common import handle_ctrl_c
from fw_common import make_sure_path_exists
from fw_common import print_banner
from fw_common import test_internet_connection
from update_version_files import get_version_files


def get_flutter_apps(filename) -> dict:
    import json
    filepath = os.path.join(filename)
    with open(filepath, 'r') as f:
        try:
            return json.load(f)
        except json.decoder.JSONDecodeError:
            print("Invalid JSON in %s" % f)
            exit(1)


def clear_folder(dir_):
    """ Clears folder specified """
    import shutil
    if os.path.exists(dir_):
        shutil.rmtree(dir_)


def get_repo(repo_path: str, output_path: str,
             uri: str, branch: str, rev: str, license_file: str, license_type: str,
             author: str,
             recipe_folder: str,
             package_output_path: str,
             ignore_list: dict,
             rdepends_list: dict,
             output_path_override_list: dict,
             src_folder: str,
             src_files: dict,
             entry_files: dict,
             variables: dict,
             patch_dir: str):
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

    if not uri:
        print("repo entry needs a 'uri' key.  Skipping")
        return
    if not branch:
        print("repo entry needs a 'branch' key.  Skipping")
        return

    # get repo folder name
    repo_name: list[str] = uri.rsplit('/', 1)[-1]
    repo_name = repo_name.split(".")
    repo_name: str = repo_name[0]

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
            exit(1)

        if license_type != 'CLOSED':
            license_md5 = get_file_md5(license_path)

    repo_path = os.path.join(repo_path, repo_name)


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
                         src_folder=src_folder,
                         src_files=src_files,
                         entry_files=entry_files,
                         variables=variables,
                         patch_dir=patch_dir)


def get_workspace_repos(repo_path, repos, output_path, package_output_path, patch_dir):
    """ Clone GIT repos referenced in config repos dict to base_folder """
    import concurrent.futures

    with concurrent.futures.ThreadPoolExecutor() as executor:
        for r in repos:
            get_repo(repo_path=repo_path,
                     output_path=output_path,
                     package_output_path=package_output_path,
                     uri=r.get('uri'),
                     branch=r.get('branch'),
                     rev=r.get('rev'),
                     license_file=r.get('license_file'),
                     license_type=r.get('license_type'),
                     author=r.get('author'),
                     recipe_folder=r.get('folder'),
                     ignore_list=r.get('ignore'),
                     rdepends_list=r.get('rdepends'),
                     output_path_override_list=r.get('output_folder'),
                     src_folder=r.get('src_folder'),
                     src_files=r.get('src_files'),
                     entry_files=r.get('entry_files'),
                     variables=r.get('variables'),
                     patch_dir=patch_dir)

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


def main():
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument('--version', default='', type=str, help='Flutter SDK version')
    parser.add_argument('--path', default='.', type=str, help='meta-flutter root path')
    parser.add_argument('--json', default='./meta-flutter-apps/conf/flutter-apps.json', type=str, help='JSON file of flutter apps')
    parser.add_argument('--patch-dir', default='./tools/patches', type=str, help='Path to patch folder')
    args = parser.parse_args()

    #
    # Control+C handler
    #
    signal.signal(signal.SIGINT, handle_ctrl_c)

    if not os.path.exists(args.path):
        make_sure_path_exists(args.path)

    include_path = os.path.join(args.path, 'conf', 'include')

    if args.version != '':
        update_flutter_version_inc(include_path, args.version)

    print_banner(f'Rolling meta-flutter')
    print_banner('Updating version files')
    get_version_files(include_path)

    print_banner('Done updating version files')

    print_banner(f'Updating meta-flutter-apps from {args.json}')
    repos = get_flutter_apps(args.json)

    repo_path = os.path.join(os.getcwd(), '.flutter-apps')
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
    check_python_version()

    if not test_internet_connection():
        sys.exit("roll_meta_flutter.py requires an internet connection")

    main()
