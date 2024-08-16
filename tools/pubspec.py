#!/usr/bin/env python3
# SPDX-FileCopyrightText: (C) 2024 Joel Winarske
# SPDX-License-Identifier: Apache-2.0
#
# pubspec.py
#   used to archive and/or restore pub cache packages
#   Restores pub cache to $PUB_CACHE
#
# Example usage
#
# source $HOME/workspace-automation/setup_env.sh
# $HOME/meta-flutter/tools/pubspec.py --project-path $HOME/workspace-automation/app \
#     --archive-path /tmp/archive/pub_cache --walk
#
# <turn off your WiFi/Ethernet>
#
# dart pub cache clean -f
# $HOME/meta-flutter/tools/pubspec.py --project-path $HOME/workspace-automation/app \
#     --archive-path /tmp/archive/pub_cache --walk --restore
#

import logging
import os
import shutil
from urllib.parse import quote
from urllib.parse import unquote
from urllib.parse import urlparse

from common import get_flutter_sdk_path
from common import make_sure_path_exists
from common import run_command

logger = logging.getLogger(__name__)


def main():
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument('--project-path', default='', type=str, help='Directory to look for pubspec.yaml files')
    parser.add_argument('--archive-path', default='./archive/pub_cache', type=str, help='Folder to store archives in')
    parser.add_argument('--restore', default=False, action='store_true', help='Project folder to restore lockfile from')
    parser.add_argument('--walk', default=False, action='store_true', help='Walk Project path for pubspec.yaml')
    parser.add_argument('--v', default=False, action='store_true', help='Verbose output')

    args = parser.parse_args()

    if args.v:
        logging.basicConfig(level=logging.DEBUG, format='(asctime)s - %(levelname)s - %(threadName)s %(message)s')
    else:
        logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

    #
    # Control+C handler
    #
    import signal
    from common import handle_ctrl_c
    signal.signal(signal.SIGINT, handle_ctrl_c)

    if not args.project_path:
        logging.error('Project path required')
        return

    if not args.archive_path:
        logging.error('Pub spec archive required')
        return

    # flutter doctor implicitly fetches this
    flutter_sdk_path = get_flutter_sdk_path()

    if not args.restore:
        pubspec_archive_packages_in_lock_file(args.project_path, args.archive_path, args.walk)
        pubspec_archive_packages_in_lock_file(os.path.join(flutter_sdk_path, 'packages'), args.archive_path, True)
        pubspec_archive_packages_in_lock_file(os.path.join(flutter_sdk_path, 'bin', 'cache', 'dart-sdk', 'pkg', '_macros'), args.archive_path)

    else:
        pubspec_restore_project_pub_cache(args.project_path, args.archive_path, args.walk)
        pubspec_restore_project_pub_cache(os.path.join(flutter_sdk_path, 'packages'), args.archive_path, True)
        pubspec_restore_project_pub_cache(os.path.join(flutter_sdk_path, 'bin', 'cache', 'dart-sdk', 'pkg', '_macros'), args.archive_path)

    logging.info("Done")


def pubspec_restore_git_archive(name: str, package: dict, project_path: str, pub_cache: str, archive_path: str):
    """
    Restore git archive
    """
    description = package['description']
    if not isinstance(description, dict):
        logging.error('description in not a dict')
        return

    logging.debug(f'Restoring git: {name}')

    pub_cache_git_path = os.path.join(pub_cache, 'git')
    pub_cache_git_cache_path = os.path.join(pub_cache_git_path, 'cache')
    make_sure_path_exists(pub_cache_git_cache_path)

    resolved_ref = description['resolved-ref']
    url = description['url']

    url_parsed = urlparse(url)
    repo_name = url_parsed.path.split('.git')[0].split('/')[-1]

    src_git_folder_name = repo_name + '-' + sha1_hash(url)
    pub_cache_git_archive_path = os.path.join(archive_path, 'git')
    pub_cache_git_cache_archive_path = os.path.join(pub_cache_git_archive_path, 'cache')
    src_path = os.path.join(pub_cache_git_cache_archive_path, src_git_folder_name)
    dest_path = os.path.join(pub_cache_git_cache_path, src_git_folder_name)

    if not os.path.exists(dest_path):
        shutil.copytree(src_path, dest_path)

    src_path = os.path.join(pub_cache_git_cache_path, src_git_folder_name)
    dest_path = os.path.join(pub_cache_git_path, repo_name + '-' + resolved_ref)
    if not os.path.exists(dest_path):
        run_command(f'git clone {src_path} {dest_path}', project_path)

    run_command(f'git checkout {resolved_ref}', dest_path)


def pubspec_restore_hosted_archive(package_name: str, package: dict, pub_cache: str, archive_path: str):
    logging.info(f'Restoring hosted: {package_name}')

    version = package['version']

    description = package['description']
    name = description['name']
    url = description['url']
    url_parsed = urlparse(url)

    hosted_path = os.path.join(pub_cache, 'hosted')
    hosted_cache_path = os.path.join(hosted_path, url_parsed.hostname, '.cache')
    make_sure_path_exists(hosted_cache_path)

    #
    # {package}-advisories.json
    #

    file = os.path.join(archive_path, url_parsed.hostname, '.cache', name + '-advisories.json')
    if os.path.exists(file):
        shutil.copy(str(file), hosted_cache_path)

    #
    # {package}-versions.json
    #

    file = os.path.join(archive_path, url_parsed.hostname, '.cache', name + '-versions.json')
    if not os.path.exists(file):
        logging.error(f'Missing: {file}')
        raise FileNotFoundError

    shutil.copy(str(file), hosted_cache_path)

    #
    # archive name
    #

    filename = name + '-' + version + '.tar.gz'
    file = os.path.join(archive_path, url_parsed.hostname, filename)
    if not os.path.exists(file):
        logging.error(f'Missing: {file}')
        raise FileNotFoundError

    restore_folder = os.path.join(hosted_path, url_parsed.hostname, name + '-' + version)
    make_sure_path_exists(restore_folder)
    run_command(f'tar -xzf {file} -C {restore_folder}', restore_folder, True)

    #
    # .sha256 file
    #
    hosted_hashes_path = os.path.join(pub_cache, 'hosted-hashes')

    filename += '.sha256'
    file = os.path.join(archive_path, url_parsed.hostname, filename)
    if not os.path.exists(file):
        logging.error(f'Missing: {file}')
        raise FileNotFoundError

    bare_filename = filename[:-14]
    hostname_path = os.path.join(hosted_hashes_path, url_parsed.hostname)
    make_sure_path_exists(hostname_path)
    file_dest = os.path.join(hostname_path, bare_filename + '.sha256')

    logging.debug(f'sha256: {file_dest}')
    shutil.copy(str(file), file_dest)


def pubspec_restore_project_pub_cache(base_path: str, archive_path: str, walk: bool = False):
    """
    Create pub cache for specified project path
    """
    pub_cache_path = os.getenv('PUB_CACHE')
    if not pub_cache_path:
        logging.error("PUB_CACHE is not set")
        return

    for dir_path, _, filenames in os.walk(base_path):
        for filename in filenames:
            if filename == 'pubspec.lock':
                if not os.path.exists(archive_path):
                    logging.error(f'Path does not exist: {archive_path}')
                    raise FileNotFoundError

                logging.info(f'Restoring {dir_path}/{filename} to {pub_cache_path}')

                pub_cache = os.environ.get('PUB_CACHE', None)
                if not pub_cache:
                    logging.error('PUB_CACHE is not set.  Cannot restore')
                    continue

                packages = get_yaml_obj(f'{dir_path}/{filename}')['packages']
                for name in packages:
                    package = packages[name]

                    source = package['source']
                    logging.debug(f'package {name}: source: {source}')
                    if source == 'sdk':
                        logging.debug('Skipping')
                        continue
                    elif source == 'path':
                        logging.debug('Skipping')
                        continue
                    elif source == 'git':
                        pubspec_restore_git_archive(name, package, dir_path, pub_cache, archive_path)
                        continue
                    elif source == 'hosted':
                        pubspec_restore_hosted_archive(name, package, pub_cache, archive_path)
                        continue
                    else:
                        logging.error(f'{name}: Unknown source type: {source}')
                        continue
                break
        if not walk:
            break


def pubspec_hosted_archive_exists(name: str, url: str, version: str, output_path: str) -> bool:
    """
    Check if hosted files exists
    """

    url_parse_res = urlparse(url)
    hostname_path = os.path.join(output_path, url_parse_res.hostname)

    archive_file = os.path.join(hostname_path, name + '-' + version + '.tar.gz')

    if os.path.exists(archive_file):
        return True

    return False


def pubspec_archive_hosted(package_name: str, package: dict, output_path: str):
    from datetime import datetime

    logging.debug(package_name)
    logging.debug(f'{package}')

    version = package['version']

    description = package['description']
    if not isinstance(description, dict):
        logging.error('Invalid description')
        return

    desc_name = description['name']
    url = description['url']

    url_parse_res = urlparse(url)
    hostname_path = os.path.join(output_path, url_parse_res.hostname)
    hostname_cache_path = os.path.join(hostname_path, '.cache')

    #
    # Check advisories
    #
    advisories = pubspec_get_package_advisories(package_name, url)
    # '{"advisories":[],"advisoriesUpdated":null}'
    if len(advisories) > 42:
        logging.debug(f'{package_name} has advisories')
        advisories_file_path = os.path.join(hostname_cache_path, package_name + '-advisories.json')
        with open(advisories_file_path, 'w') as f:
            import fcntl
            fcntl.lockf(f, fcntl.LOCK_EX)
            f.write(advisories)
            fcntl.lockf(f, fcntl.LOCK_UN)

    #
    # Fetch {package}-versions.json file
    #
    version_file_path = os.path.join(hostname_cache_path, package_name + '-versions.json')
    if not os.path.exists(version_file_path):
        make_sure_path_exists(hostname_cache_path)

        versions = pubspec_get_package_versions(package_name, url)
        timestamp = datetime.now().strftime("%Y-%m-%dT%H:%M:%S.%f")

        versions = versions[:-1] + ',"_fetchedAt":"' + timestamp + '"}'

        with open(version_file_path, 'w') as f:
            import fcntl
            fcntl.lockf(f, fcntl.LOCK_EX)
            f.write(versions)
            fcntl.lockf(f, fcntl.LOCK_UN)

    #
    # Fetch archive file
    #
    if not pubspec_hosted_archive_exists(desc_name, url, version, output_path):

        versions = get_versions_obj(version_file_path)

        package = None
        for v in versions['versions']:
            if v['version'] == version:
                package = v
                break

        if not package:
            logging.warning(f'{package_name}: {version} not found in {version_file_path}')

        else:
            url = package['archive_url']
            logging.debug(f'url: {url}')
            sha256 = package['archive_sha256']
            url_parse_res = urlparse(url)

            file = unquote(os.path.basename(url_parse_res.path))

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
        message_digest = hashlib.sha1()
        message_digest.update(bytes(to_hash, 'utf'))
        return message_digest.hexdigest()
    except TypeError:
        raise "String to hash was not compatible"


def pubspec_archive_git(name: str, package: dict, project_dir: str, archive_path: str):
    logging.debug(name)
    logging.debug(f'{package}')

    description = package['description']
    if not isinstance(description, dict):
        logging.error('Fail!')
        return

    resolved_ref = description['resolved-ref']
    url = description['url']

    url_parsed = urlparse(url)
    repo_name = url_parsed.path.split('.git')[0].split('/')[-1]

    git_archive_path = os.path.join(archive_path, 'git', 'cache')
    make_sure_path_exists(git_archive_path)

    git_folder_name = repo_name + '-' + sha1_hash(url)

    git_folder = os.path.join(git_archive_path, git_folder_name)
    if os.path.exists(git_folder):
        return

    run_command(f'git clone --mirror {url} {git_folder}', project_dir, True)
    run_command(f'git --git-dir={git_folder} rev-list --max-count=1 {resolved_ref}', git_folder, True)
    run_command(f'git --git-dir={git_folder} show {resolved_ref}:pubspec.yaml', git_folder, True)


def pubspec_archive_package(name: str, package: dict, project_path: str, output_path: str):
    """
    Fetches an archive file if source is hosted, and not already present in archive
    """

    source = package.get('source', '')
    if source == 'sdk':
        logging.debug(f'Skipping: {package}')
        return
    elif source == 'path':
        logging.debug(f'Skipping: {package}')
        return
    elif source == 'git':
        pubspec_archive_git(name, package, project_path, output_path)
        return
    elif source == 'hosted':
        pubspec_archive_hosted(name, package, output_path)
        return
    else:
        logging.error(f'{name}: Unknown source type: {source}')
        return


def pubspec_archive_packages_in_lock_file(base_path: str, output_path: str, walk: bool = False):
    """
    Archive pubspec packages for a given 'pubspec.lock' file
    """
    import concurrent.futures

    fs = []
    with concurrent.futures.ThreadPoolExecutor() as executor:
        for dir_path, _, filenames in os.walk(base_path):
            if 'pubspec.yaml' in filenames and 'pubspec.lock' not in filenames:
                # create the lock file
                fs.append(executor.submit(run_command,'dart pub get', dir_path, False))
            if not walk:
                break

    concurrent.futures.wait(fs, timeout=None, return_when=concurrent.futures.ALL_COMPLETED)

    fs = []
    with concurrent.futures.ThreadPoolExecutor() as executor:
        for dir_path, _, filenames in os.walk(base_path):
            for filename in filenames:
                if filename == 'pubspec.lock':

                    logging.info(f'Archiving {dir_path}/{filename}')

                    # iterate pubspec.lock file
                    pubspec_lock = get_yaml_obj(f'{dir_path}/{filename}')
                    packages = pubspec_lock['packages']

                    for name in packages:
                        fs.append(
                            executor.submit(pubspec_archive_package, name, package=packages[name],
                                            project_path=dir_path,
                                            output_path=output_path))
                    break
            if not walk:
                break

    concurrent.futures.wait(fs, timeout=None, return_when=concurrent.futures.ALL_COMPLETED)


def pubspec_get_package_advisories(name: str, url: str) -> str:
    """
    Return version dict for a specified description
    """
    import io
    import pycurl

    if url == '':
        logging.warning(f'Missing url in description, using default')
        url = "https://pub.dev"

    endpoint = '/api/packages/' + name + '/advisories'
    url = url + quote(endpoint)
    logging.debug(f'GET: {url}')

    c = pycurl.Curl()
    c.setopt(pycurl.URL, url)
    c.setopt(pycurl.HTTPHEADER, ["Accept: application/vnd.pub.v2+json"])
    buffer = io.BytesIO()
    c.setopt(pycurl.WRITEDATA, buffer)
    c.perform()
    return buffer.getvalue().decode('utf-8')


def pubspec_get_package_versions(name: str, url: str) -> str:
    """
    Return version dict for a specified description
    """
    import io
    import pycurl

    if url == '':
        logging.warning(f'Missing url in description, using default')
        url = "https://pub.dev"

    endpoint = '/api/packages/' + name
    url = url + quote(endpoint)
    logging.debug(f'GET: {url}')

    c = pycurl.Curl()
    c.setopt(pycurl.URL, url)
    c.setopt(pycurl.HTTPHEADER, ["Accept: application/vnd.pub.v2+json"])
    buffer = io.BytesIO()
    c.setopt(pycurl.WRITEDATA, buffer)
    c.perform()
    return buffer.getvalue().decode('utf-8')


def pubspec_get_package_version(desc_name: str, desc_url: str, version: str) -> dict:
    """
    Return version dict for a specified description
    """
    import io
    import json
    import pycurl

    if desc_url == '':
        logging.warning(f'Missing url in description, using default')
        desc_url = "https://pub.dev"

    endpoint = '/api/packages/' + desc_name + '/versions/' + version
    url = desc_url + quote(endpoint)
    logging.debug(f'GET: {url}')

    c = pycurl.Curl()
    c.setopt(pycurl.URL, url)
    c.setopt(pycurl.HTTPHEADER, ["Accept: application/vnd.pub.v2+json"])
    buffer = io.BytesIO()
    c.setopt(pycurl.WRITEDATA, buffer)
    c.perform()
    return json.loads(buffer.getvalue().decode('utf-8'))


def get_versions_obj(file_path: str) -> dict:
    """
    Returns dictionary of versions JSON
    :param file_path:
    :return: dict
    """
    import json

    try:
        with open(file_path, 'r') as file:
            data = json.load(file)
        return data
    except FileNotFoundError:
        logging.error(f"The file {file_path} does not exist.")
        return dict()
    except json.JSONDecodeError:
        logging.error(f"There was an error decoding the JSON from the file {file_path}")
        return dict()
    except Exception as e:
        logging.error(f"An unexpected error occurred: {e}")
        return dict()


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
            logging.error(f'Failed loading: {filepath}')
            return dict()

        return data_loaded


if __name__ == "__main__":
    from common import check_python_version

    check_python_version()

    main()
