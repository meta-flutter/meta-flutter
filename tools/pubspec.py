#!/usr/bin/env python3
# SPDX-FileCopyrightText: (C) 2024 Joel Winarske
# SPDX-License-Identifier: Apache-2.0
#
# pubspec.py
#   archives project using --input path
#   restores pub_cache using --restore <path to project>.  Writes to $PUB_CACHE
#   If a project is missing a pubspec.lock file, it gets created.
#
#   source types hosted and git are supported.
#   source types sdk and path are skipped.
#
# Example usage:
#   cd $HOME/workspace-automation/app/flutter-wonderous-app/
#   flutter pub upgrade
#   tools/pubspec.py --input $HOME/workspace-automation/flutter/packages/flutter_tools --output $(pwd)/archive/pub_cache
#   tools/pubspec.py --input $HOME/workspace-automation/app/flutter-wonderous-app/ --output $(pwd)/archive/pub_cache
#   <turn off your WiFi/Ethernet>
#   dart pub cache clean -f
#   tools/pubspec.py --restore $HOME/workspace-automation/app/flutter-wonderous-app/ --output $(pwd)/archive/pub_cache
#   tools/pubspec.py --restore $HOME/workspace-automation/flutter/packages/flutter_tools --output $(pwd)/archive/pub_cache
#   cd $HOME/workspace-automation/app/flutter-wonderous-app/
#   flutter build bundle -v
#

import os

from common import make_sure_path_exists
from common import print_banner
from common import run_command
from urllib.parse import quote
from urllib.parse import unquote
from urllib.parse import urlparse

import shutil


def main():
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument('--input', default='', type=str, help='Directory to look for pubspec.yaml files')
    parser.add_argument('--output', default='./archive/pub_cache', type=str, help='Folder to store archives in')
    parser.add_argument('--restore', default='', type=str, help='Project folder to restore lockfile from')
    args = parser.parse_args()

    #
    # Control+C handler
    #
    import signal
    from common import handle_ctrl_c
    signal.signal(signal.SIGINT, handle_ctrl_c)

    if args.input != '':
        pubspec_archive_lock_file(args.input, args.output)

    if args.restore:
        pubspec_restore_project_pub_cache(args.restore, args.output)

    print_banner("Done")


def pubspec_restore_archive(archive_path: str):
    """
    Restores an archive folder into a pub_cache
    """

    if not os.path.exists(archive_path):
        return

    pub_cache = os.environ.get('PUB_CACHE', None)
    if not pub_cache:
        print_banner('PUB_CACHE is not set.  Cannot restore')
        return

    hosted_path = os.path.join(pub_cache, 'hosted')
    hosted_hashes_path = os.path.join(pub_cache, 'hosted-hashes')

    # create required folders
    make_sure_path_exists(hosted_path)
    make_sure_path_exists(hosted_hashes_path)

    for root, dirs, files in os.walk(archive_path):
        for name in files:
            if name.endswith('.tar.gz'):
                import tarfile

                file = os.path.join(root, name)
                folder_name = name[:-7]
                hostname = os.path.basename(os.path.dirname(file))
                restore_folder = os.path.join(hosted_path, hostname, folder_name)
                print(f'folder: {restore_folder}')

                # extract package
                f = tarfile.open(file)
                f.extractall(restore_folder)

            elif name.endswith('.sha256'):
                file = os.path.join(root, name)
                bare_filename = name[:-14]
                hostname = os.path.basename(os.path.dirname(file))
                hostname_path = os.path.join(hosted_hashes_path, hostname)
                make_sure_path_exists(hostname_path)
                file_dest = os.path.join(hostname_path, bare_filename + '.256')
                print(f'sha256: {file_dest}')
                shutil.copy(str(file), file_dest)


def pubspec_restore_git_archive(name: str, package: dict, project_path: str, pub_cache: str, archive_path: str):
    description = package['description']
    if not isinstance(description, dict):
        print_banner('Fail!')
        return

    print_banner(f'Restoring git: {name}')

    pub_cache_git_archive_path = os.path.join(archive_path, 'git')
    pub_cache_git_path = os.path.join(pub_cache, 'git')
    pub_cache_git_cache_path = os.path.join(pub_cache, 'git', 'cache')
    make_sure_path_exists(pub_cache_git_path)

    resolved_ref = description['resolved-ref']
    url = description['url']

    url_parsed = urlparse(url)
    repo_name = url_parsed.path.split('.git')[0].split('/')[-1]
    src_git_folder_name = repo_name + '-' + sha1_hash(url)
    dest_git_folder_name = repo_name + '-' + resolved_ref

    src_path = os.path.join(pub_cache_git_archive_path, src_git_folder_name)
    dest_path = os.path.join(pub_cache_git_cache_path, dest_git_folder_name)

    shutil.copytree(src_path, dest_path)

    src_path = dest_path
    dest_path = os.path.join(pub_cache_git_path, dest_git_folder_name)
    run_command(f'git clone {src_path} {dest_path}', project_path)
    run_command(f'git checkout {resolved_ref}', dest_path)


def pubspec_restore_hosted_archive(name: str, package: dict, pub_cache: str, archive_path: str):
    import tarfile

    print_banner(f'Restoring hosted: {name}')

    hosted_path = os.path.join(pub_cache, 'hosted')
    hosted_hashes_path = os.path.join(pub_cache, 'hosted-hashes')

    version = package['version']

    description = package['description']
    name = description['name']
    url = description['url']
    url_parsed = urlparse(url)

    filename = name + '-' + version + '.tar.gz'
    file = os.path.join(archive_path, url_parsed.hostname, filename)
    if not os.path.exists(file):
        print(f'Missing: {file}')
        raise FileNotFoundError

    restore_folder = os.path.join(hosted_path, url_parsed.hostname, name + '-' + version)

    print(f'folder: {restore_folder}')
    f = tarfile.open(file)
    f.extractall(restore_folder)

    filename += '.sha256'
    file = os.path.join(archive_path, url_parsed.hostname, filename)
    if not os.path.exists(file):
        print(f'Missing: {file}')
        raise FileNotFoundError

    bare_filename = filename[:-14]
    hostname_path = os.path.join(hosted_hashes_path, url_parsed.hostname)
    make_sure_path_exists(hostname_path)
    file_dest = os.path.join(hostname_path, bare_filename + '.sha256')

    print(f'sha256: {file_dest}')
    shutil.copy(str(file), file_dest)


def pubspec_restore_project_pub_cache(project_path: str, archive_path: str):

    lock_file = os.path.join(project_path, 'pubspec.lock')
    if not os.path.exists(lock_file):
        print_banner(f'file not found: {lock_file}')
        raise FileNotFoundError

    if not os.path.exists(archive_path):
        print_banner(f'Path does not exist: {archive_path}')
        raise FileNotFoundError

    pub_cache = os.environ.get('PUB_CACHE', None)
    if not pub_cache:
        print_banner('PUB_CACHE is not set.  Cannot restore')
        return

    packages = get_yaml_obj(lock_file)['packages']
    for name in packages:
        package = packages[name]

        source = package['source']
        print_banner(f'package {name}: source: {source}')
        if source == 'sdk':
            print('Skipping')
            continue
        elif source == 'path':
            print('Skipping')
            continue
        elif source == 'git':
            pubspec_restore_git_archive(name, package, project_path, pub_cache, archive_path)
            continue
        elif source == 'hosted':
            pubspec_restore_hosted_archive(name, package, pub_cache, archive_path)
            continue
        else:
            print_banner(f'{name}: Unknown source type: {source}')
            continue


def hosted_archive_file_exists(name: str, url: str, version: str, output_path: str) -> bool:
    """
    Check if archive file exists
    """

    url_parse_res = urlparse(url)
    hostname_path = os.path.join(output_path, url_parse_res.hostname)

    archive_file = os.path.join(hostname_path, name + '-' + version + '.tar.gz')

    if os.path.exists(archive_file):
        return True

    return False


def archive_hosted(name: str, package: dict, output_path: str):

    print_banner(name)
    print(f'{package}')

    version = package['version']

    description = package['description']
    if not isinstance(description, dict):
        print_banner('Fail!')
        return

    name = description['name']
    url = description['url']

    if hosted_archive_file_exists(name, url, version, output_path):
        return

    package_json = pubspec_get_package_version(name, url, version)
    if not bool(package_json):
        return

    if package_json['version'] == version:
        url = package_json['archive_url']
        print(f'url: {url}')
        sha256 = package_json['archive_sha256']
        url_parse_res = urlparse(url)

        file = unquote(os.path.basename(url_parse_res.path))

        hostname_path = os.path.join(output_path, url_parse_res.hostname)
        make_sure_path_exists(hostname_path)

        archive_file = os.path.join(hostname_path, file)
        if not os.path.exists(archive_file):
            from common import download_https_file
            download_https_file(hostname_path, url, file, None, None, None, None, sha256)


def git_archive_exists(archive_path: str, folder_name: str) -> bool:
    """
    Check if git archive exists
    """

    archive_file = os.path.join(archive_path, folder_name)

    if os.path.exists(archive_file):
        return True

    return False


def sha1_hash(to_hash: str) -> str:
    import hashlib
    try:
        messageDigest = hashlib.sha1()
        messageDigest.update(bytes(to_hash, 'utf'))
        return messageDigest.hexdigest()
    except TypeError:
        raise "String to hash was not compatible"


def pubspec_archive_git(name: str, package: dict, project_dir: str, output_path: str):

    print_banner(name)
    print(f'{package}')

    description = package['description']
    if not isinstance(description, dict):
        print_banner('Fail!')
        return

    resolved_ref = description['resolved-ref']
    url = description['url']

    url_parsed = urlparse(url)
    repo_name = url_parsed.path.split('.git')[0].split('/')[-1]

    git_folder_name = repo_name + '-' + sha1_hash(url)

    git_archive_path = os.path.join(output_path, 'git')

    if git_archive_exists(git_archive_path, git_folder_name):
        return

    make_sure_path_exists(git_archive_path)

    git_folder = os.path.join(git_archive_path, git_folder_name)

    run_command(f'git clone --mirror {url} {git_folder}', project_dir)
    run_command(f'git --git-dir={git_folder} rev-list --max-count=1 {resolved_ref}', git_folder)
    run_command(f'git --git-dir={git_folder} show {resolved_ref}:pubspec.yaml', git_folder)


def pubspec_archive_package(name: str, package: dict, project_path: str, output_path: str):
    """
    Fetches an archive file if source is hosted, and not already present in archive
    """

    source = package.get('source', '')
    if source == '':
        print(f'Skipping: {package}')
        return
    elif source == 'sdk':
        print(f'Skipping: {package}')
        return
    elif source == 'path':
        print(f'Skipping: {package}')
        return
    elif source == 'git':
        pubspec_archive_git(name, package, project_path, output_path)
        return
    elif source == 'hosted':
        archive_hosted(name, package, output_path)
        return
    else:
        print(f'{name}: Unknown source type: {source}')
        return


def pubspec_archive_lock_file(project_path: str, output_path: str):
    """
    Archive all pubspec packages in a given 'pubspec.lock' file
    """

    # check for pubspec.yaml
    pubspec_yaml_path = os.path.join(project_path, 'pubspec.yaml')
    if not os.path.exists(pubspec_yaml_path):
        print_banner(f'file not found: {pubspec_yaml_path}')
        raise FileNotFoundError

    # check or pubspec.lock
    pubspec_lock_path = os.path.join(project_path, 'pubspec.lock')
    if not os.path.exists(pubspec_lock_path):
        import subprocess

        (result, output) = subprocess.getstatusoutput(f'cd {project_path} && dart pub get')
        if result == 65:
            print(f'Failed to get pub cache for: {project_path}')
            print(get_yaml_obj(pubspec_yaml_path))
            return
        print(output)

        from common import run_command
        run_command('dart pub cache clean -f', output_path)

    if not os.path.exists(pubspec_lock_path):
        print_banner(f'dart pub get failed for {project_path}')
        return

    # iterate pubspec.lock file
    pubspec_lock = get_yaml_obj(pubspec_lock_path)

    packages = pubspec_lock['packages']

    for name in packages:
        pubspec_archive_package(name, package=packages[name], project_path=project_path, output_path=output_path)


def pubspec_archive_pubspec_yaml_packages(input_path: str, output_path: str):
    """
    Archives all pubspec packages under the input_path directory
    """

    if not os.path.exists(input_path):
        print_banner(f'Invalid input path: {input_path}')
        return

    if not output_path:
        output_path = os.getcwd()

    make_sure_path_exists(output_path)

    for root, dirs, files in os.walk(input_path):
        for file in files:
            # skip any files with pub_cache in path
            if 'pub_cache' in file:
                continue
            if file.endswith('pubspec.yaml'):
                pubspec_archive_lock_file(root, output_path)
                print(f'Done: {os.path.join(root, file)}')


def pubspec_get_package_version(desc_name: str, desc_url: str, version: str) -> dict:
    """
    Return version dict for a specified description
    """
    import io
    import json
    import pycurl

    if desc_url == '':
        print(f'Missing url in description, using default')
        desc_url = "https://pub.dev"

    endpoint = '/api/packages/' + desc_name + '/versions/' + version
    url = desc_url + quote(endpoint)
    print(f'GET: {url}')

    c = pycurl.Curl()
    c.setopt(pycurl.URL, url)
    c.setopt(pycurl.HTTPHEADER, ["Accept: application/vnd.pub.v2+json"])
    buffer = io.BytesIO()
    c.setopt(pycurl.WRITEDATA, buffer)
    c.perform()
    return json.loads(buffer.getvalue().decode('utf-8'))


def get_yaml_obj(filepath: str) -> dict:
    """
    Returns python object of yaml file
    """
    import yaml

    if not os.path.exists(filepath):
        raise FileNotFoundError

    with open(filepath, "r") as stream_:
        try:
            data_loaded = yaml.full_load(stream_)

        except yaml.YAMLError:
            # print(f'Failed loading {exc} - {filepath}')
            return dict()

        return data_loaded


if __name__ == "__main__":
    from common import check_python_version

    check_python_version()

    main()
