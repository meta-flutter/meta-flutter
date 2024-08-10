#!/usr/bin/env python3
#
# SPDX-FileCopyrightText: (C) 2024 Joel Winarske
#
# SPDX-License-Identifier: Apache-2.0
#
# Functional test case
#   Archive all pubspec packages referenced in the flutter sdk source tree
#   If a project is does not have a pubspec.lock file, it will get created
#
# Test sequence:
#   flutter pub cache clean -f
#   tools/pubspec.py --output `pwd`/archive
#   cd <flutter sdk root>/examples/hello_world
#   flutter pub get --offline
#
# the packages should all resolve quickly without using the internet
#

import concurrent.futures
import os
import io
import json
import pycurl
import shutil
import signal
import subprocess
import sys
import tarfile

from fw_common import handle_ctrl_c
from fw_common import make_sure_path_exists
from fw_common import print_banner
from fw_common import check_python_version
from fw_common import download_https_file
from fw_common import get_flutter_sdk_path
from fw_common import run_command
from urllib.parse import urlparse


def main():
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument('--output', default='./archive', type=str, help='Archive Output')
    args = parser.parse_args()

    #
    # Control+C handler
    #
    signal.signal(signal.SIGINT, handle_ctrl_c)

    archive_flutter_sdk_pub_packages(args.output)

    restore_pub_cache(args.output)

    print_banner("Done")


def restore_pub_cache(archive_path: str):
    """Function to restore an archive folder into a pub_cache"""

    pub_cache = os.environ.get('PUB_CACHE', None)
    if not pub_cache:
        print_banner('PUB_CACHE is not set.  Cannot restore')
        return

    hosted_path = os.path.join(pub_cache, 'hosted')
    hosted_hashes_path = os.path.join(pub_cache, 'hosted-hashes')
    pub_cache_tmp_path = os.path.join(pub_cache, '_temp')

    # create folder structure
    make_sure_path_exists(pub_cache)
    make_sure_path_exists(hosted_path)
    make_sure_path_exists(hosted_hashes_path)
    make_sure_path_exists(pub_cache_tmp_path)

    for root, dirs, files in os.walk(archive_path):
        for name in files:
            if name.endswith('.tar.gz'):
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
                shutil.copy(file, file_dest)


def archive_file_exists(name: str, url: str, version: str, output_path: str) -> bool:
    """Function to check if archive file exists"""

    url_parse_res = urlparse(url)
    hostname_path = os.path.join(output_path, url_parse_res.hostname)

    archive_file = os.path.join(hostname_path, name + '-' + version + '.tar.gz')

    if os.path.exists(archive_file):
        return True

    return False


def archive_package(package: dict, output_path: str):
    """Function that fetches an archive file if not present"""
    description = package['description']
    name = description['name']
    url = description['url']
    version = package['version']

    if not isinstance(description, dict):
        return

    if archive_file_exists(name, url, version, output_path):
        return

    versions_json = get_package_all_versions(description)
    if not bool(versions_json):
        return
    
    versions = versions_json['versions']
    for it in versions:
        if it['version'] == version:
            url = it['archive_url']
            sha256 = it['archive_sha256']
            url_parse_res = urlparse(url)
            file = os.path.basename(url_parse_res.path)

            hostname_path = os.path.join(output_path, url_parse_res.hostname)
            make_sure_path_exists(hostname_path)

            archive_file = os.path.join(hostname_path, file)
            if not os.path.exists(archive_file):
                download_https_file(hostname_path, url, file, None, None, None, None, sha256)


def archive_pubspec_lock(project_path: str, output_path: str):
    """Function that will archive all pubspec packages in a given pubspec.lock"""

    # check for pubspec.yaml
    pubspec_yaml_path = os.path.join(project_path, 'pubspec.yaml')
    if not os.path.exists(pubspec_yaml_path):
        sys.exit('pubspec.yaml not found')

    # check or pubspec.lock
    pubspec_lock_path = os.path.join(project_path, 'pubspec.lock')
    if not os.path.exists(pubspec_lock_path):
        (retval, output) = subprocess.getstatusoutput(f'cd {project_path} && flutter pub get')
        if retval == 65:
            print(f'Failed to get pub cache for: {project_path}')
            return
        run_command('flutter pub cache clean -f', output_path)

    if not os.path.exists(pubspec_lock_path):
        sys.exit(f'flutter pub get failed for {project_path}')

    # iterate pubspec.lock file
    pubspec_lock = get_yaml_obj(pubspec_lock_path)

    packages = pubspec_lock['packages']
    for it in packages:
        with concurrent.futures.ThreadPoolExecutor() as executor:
            futures = []
            futures.append(executor.submit(archive_package, package=packages[it], output_path=output_path))


def archive_flutter_sdk_pub_packages(output_path: str):
    """Function that will archive all pubspec packages in the flutter SDK source tree"""
    flutter_sdk_path = get_flutter_sdk_path()
    if flutter_sdk_path is None:
        sys.exit('add flutter to your path')

    if not output_path:
        output_path = os. getcwd()

    for root, dirs, files in os.walk(flutter_sdk_path):
        for file in files:
            if file.endswith('pubspec.yaml'):
                archive_pubspec_lock(root, output_path)
                print(f'Done: {os.path.join(root, file)}')


def get_package_all_versions(description: dict) -> dict:
    """Function to return all versions of a described package"""

    desc_name = description.get('name', None)
    if desc_name is None:
        print(f'Malformed: {description}')
        return dict()

    desc_url = description.get('url', None)
    if desc_url is None:
        print(f'Missing url in description, using default')
        desc_url = "https://pub.dev"

    url = desc_url + '/api/packages/' + desc_name
    print(f'url: {url}')

    c = pycurl.Curl()
    c.setopt(pycurl.URL, url)
    c.setopt(pycurl.HTTPHEADER, [ "Accept: application/vnd.pub.v2+json"])
    buffer = io.BytesIO()
    c.setopt(pycurl.WRITEDATA, buffer)
    c.perform()
    return json.loads(buffer.getvalue().decode('utf-8'))


def get_yaml_obj(filepath: str):
    """ Returns python object of yaml file """
    import yaml

    if not os.path.exists(filepath):
        sys.exit(f'Failed loading {filepath}')

    with open(filepath, "r") as stream_:
        try:
            data_loaded = yaml.full_load(stream_)

        except yaml.YAMLError as exc:
            # print(f'Failed loading {exc} - {filepath}')
            return []

        return data_loaded


if __name__ == "__main__":
    check_python_version()

    main()
