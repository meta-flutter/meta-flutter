#!/usr/bin/env python3
# SPDX-FileCopyrightText: (C) 2020-2024 Joel Winarske
# SPDX-License-Identifier: Apache-2.0
#
# Script to build custom Flutter AOT artifacts for Release and Profile runtime
#

import json
import os
import signal
import sys

from common import print_banner


def main():
    import argparse

    parser = argparse.ArgumentParser()
    parser.add_argument('--path', default='', type=str,
                        help='Create JSON files correlating Flutter SDK to Engine and Dart commits')
    args = parser.parse_args()

    if args.path == '':
        sys.exit("Must specify value for --path")

    #
    # Control+C handler
    #
    from common import handle_ctrl_c
    signal.signal(signal.SIGINT, handle_ctrl_c)

    #
    # Version Files
    #
    if args.path:
        print_banner("Creating Version files")
        get_version_files(args.path)
        return


def get_version_files(cwd):
    """Get Dart and Engine Version files"""
    import concurrent.futures

    if cwd is None:
        return
    else:
        from common import make_sure_path_exists
        make_sure_path_exists(cwd)

    release_linux = get_linux_release_file(cwd)

    res = {}
    with open(release_linux, 'r') as f:
        for release in json.load(f).get('releases', []):
            if 'dart_sdk_version' in release:
                res[release['version']] = release['dart_sdk_version']

    dest_file = os.path.join(cwd, 'dart-revision.json')
    print_banner("Writing %s" % dest_file)
    with open(dest_file, 'w+') as o:
        json.dump(res, o, sort_keys=True, indent=2)

    print_banner('Fetching Engine revisions')
    engine_revs = {}
    with concurrent.futures.ThreadPoolExecutor() as executor:
        futures = []
        with open(release_linux, 'r') as f:
            for release in json.load(f).get('releases', []):
                version_ = release['version']
                hash_ = release['hash']
                futures.append(executor.submit(get_engine_commit, version_, hash_))

            for future in concurrent.futures.as_completed(futures):
                res = future.result()
                engine_revs[res[0]] = res[1]

    dest_file = os.path.join(cwd, 'engine-revision.json')
    print_banner("Writing %s" % dest_file)
    with open(dest_file, 'w+') as o:
        json.dump(engine_revs, o, sort_keys=True, indent=2)

    os.remove(os.path.join(cwd, 'releases_linux.json.sha256'))

    print_banner("Done")


def get_linux_release_file(cwd):
    """Returns dictionary of releases_linux.json"""

    filename = 'releases_linux.json'
    url = f'https://storage.googleapis.com/flutter_infra_release/releases/{filename}'

    sha256_file = os.path.join(cwd, f'{filename}.sha256')
    if os.path.exists(sha256_file):
        os.remove(sha256_file)

    from common import download_https_file
    download_https_file(cwd, url, filename, None, None, None, None, None)

    return os.path.join(cwd, filename)


def get_engine_commit(version, hash_):
    """Get matching engine commit hash."""
    import pycurl
    import certifi
    from io import BytesIO

    buffer = BytesIO()
    c = pycurl.Curl()
    c.setopt(
        pycurl.URL, f'https://raw.githubusercontent.com/flutter/flutter/{hash_}/bin/internal/engine.version')
    c.setopt(pycurl.WRITEDATA, buffer)
    c.setopt(pycurl.CAINFO, certifi.where())
    c.perform()
    c.close()

    get_body = buffer.getvalue()

    return version, get_body.decode('utf8').strip()


if __name__ == "__main__":
    from common import check_python_version
    from common import test_internet_connection
    
    check_python_version()

    if not test_internet_connection():
        sys.exit("version_files.py requires an internet connection")

    main()
