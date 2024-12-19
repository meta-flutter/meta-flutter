#!/usr/bin/env python3
# SPDX-FileCopyrightText: (C) 2020-2024 Joel Winarske
# SPDX-License-Identifier: Apache-2.0

import errno
import os
import subprocess
import re
import sys
import hashlib

from sys import stderr as stream

# use kiB's
KB = 1024


def check_python_version():
    if sys.version_info[1] < 7:
        sys.exit('Python >= 3.7 required.  This machine is running 3.%s' %
                 sys.version_info[1])


def print_banner(text):
    print('*' * (len(text) + 6))
    print("** %s **" % text)
    print('*' * (len(text) + 6))


def handle_ctrl_c(_signal, _frame):
    sys.exit("Ctl+C - Closing")


def run_command(cmd: str, cwd: str, quiet: bool = False) -> str:
    cmd = re.sub('\\s{2,}', ' ', cmd)
    if not quiet:
        print('Running [%s] in %s' % (cmd, cwd))
    (result, output) = subprocess.getstatusoutput(f'cd {cwd} && {cmd}')
    if result:
        out = output.rstrip()
        sys.exit(f'failed {result} (cmd was {cmd}):\n{out}')
    return output.rstrip()


def make_sure_path_exists(path: str):
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise


def hash_file(file: str, hash_obj):
    if not os.path.exists(file):
        return ''
    with open(file, "rb") as f:
        for byte_block in iter(lambda: f.read(4096), b""):
            hash_obj.update(byte_block)
    return hash_obj.hexdigest()


def get_md5sum(file: str) -> str:
    return hash_file(file, hashlib.md5())


def get_sha1sum(file: str) -> str:
    return hash_file(file, hashlib.sha1())


def get_sha256sum(file: str):
    return hash_file(file, hashlib.sha256())


def download_https_file(cwd, url, file, cookie_file, netrc, md5, sha1, sha256):
    download_filepath = os.path.join(cwd, file)

    sha256_file = os.path.join(cwd, file + '.sha256')
    if compare_sha256(str(download_filepath), str(sha256_file)):
        print("%s exists, skipping download" % download_filepath)
        return True

    if os.path.exists(download_filepath):
        if md5:
            # don't download if md5 is good
            if md5 == get_md5sum(str(download_filepath)):
                print("** Using %s" % download_filepath)
                return True
            else:
                os.remove(download_filepath)
        elif sha1:
            # don't download if sha1 is good
            if sha1 == get_sha1sum(str(download_filepath)):
                print("** Using %s" % download_filepath)
                return True
            else:
                os.remove(str(download_filepath))
        elif sha256:
            # don't download if sha256 is good
            if sha256 == get_sha256sum(str(download_filepath)):
                print("** Using %s" % download_filepath)
                return True
            else:
                os.remove(str(download_filepath))

    print("** Downloading %s via %s" % (file, url))
    res = fetch_https_binary_file(
        url, download_filepath, False, None, cookie_file, netrc)
    if not res:
        os.remove(download_filepath)
        print_banner("Failed to download %s" % file)
        return False

    if os.path.exists(download_filepath):
        if md5:
            expected_md5 = get_md5sum(str(download_filepath))
            if md5 != expected_md5:
                sys.exit('Download artifact %s md5: %s does not match expected: %s' %
                         (download_filepath, md5, expected_md5))
        elif sha1:
            expected_sha1 = get_sha1sum(str(download_filepath))
            if sha1 != expected_sha1:
                sys.exit('Download artifact %s sha1: %s does not match expected: %s' %
                         (download_filepath, md5, expected_sha1))
        elif sha256:
            expected_sha256 = get_sha256sum(str(download_filepath))
            if sha256 != expected_sha256:
                sys.exit('Download artifact %s sha256: %s does not match expected: %s' %
                         (download_filepath, sha256, expected_sha256))

    write_sha256_file(cwd, file)
    return True


def compare_sha256(archive_path: str, sha256_file: str) -> bool:
    if not os.path.exists(archive_path):
        return False

    if not os.path.exists(sha256_file):
        return False

    archive_sha256_val = get_sha256sum(archive_path)

    with open(sha256_file, 'r') as f:
        sha256_file_val = f.read().replace('\n', '')

        if archive_sha256_val == sha256_file_val:
            return True

    return False


def write_sha256_file(cwd: str, filename: str):
    file = os.path.join(cwd, filename)
    sha256_val = get_sha256sum(file)
    sha256_file = os.path.join(cwd, filename + '.sha256')

    with open(sha256_file, 'w+') as f:
        import fcntl
        fcntl.lockf(f, fcntl.LOCK_EX)
        f.write(sha256_val)
        fcntl.lockf(f, fcntl.LOCK_UN)


def get_yaml_obj(filepath: str):
    """ Returns python object of yaml file """
    import yaml

    if not os.path.exists(filepath):
        sys.exit(f'Failed loading {filepath}')

    with open(filepath, "r") as stream_:
        try:
            data_loaded = yaml.full_load(stream_)

        except yaml.YAMLError:
            # print(f'Failed loading {exc} - {filepath}')
            return []

        return data_loaded


def fetch_https_progress(download_t, download_d, _upload_t, _upload_d):
    """callback function for pycurl.XFERINFOFUNCTION"""
    stream.write('Progress: {}/{} kiB ({}%)\r'.format(str(int(download_d / KB)), str(int(download_t / KB)),
                                                      str(int(download_d / download_t * 100) if download_t > 0 else 0)))
    stream.flush()


def fetch_https_binary_file(url, filename, redirect, headers, cookie_file, netrc) -> bool:
    """Fetches binary file via HTTPS"""
    import pycurl
    import time

    retries_left = 3
    delay_between_retries = 5  # seconds
    success = False

    c = pycurl.Curl()
    c.setopt(pycurl.URL, url)
    c.setopt(pycurl.CONNECTTIMEOUT, 30)
    c.setopt(pycurl.NOSIGNAL, 1)
    c.setopt(pycurl.NOPROGRESS, False)
    c.setopt(pycurl.XFERINFOFUNCTION, fetch_https_progress)

    if headers:
        c.setopt(pycurl.HTTPHEADER, headers)

    if redirect:
        c.setopt(pycurl.FOLLOWLOCATION, 1)
        c.setopt(pycurl.AUTOREFERER, 1)
        c.setopt(pycurl.MAXREDIRS, 255)

    if cookie_file:
        cookie_file = os.path.expandvars(cookie_file)
        print("Using cookie file: %s" % cookie_file)
        c.setopt(pycurl.COOKIEFILE, cookie_file)

    if netrc:
        c.setopt(pycurl.NETRC, 1)

    while retries_left > 0:
        try:
            with open(filename, 'wb') as f:
                import fcntl
                fcntl.lockf(f, fcntl.LOCK_EX)
                c.setopt(pycurl.WRITEFUNCTION, f.write)
                c.perform()
                fcntl.lockf(f, fcntl.LOCK_UN)

            success = True
            break

        except pycurl.error:
            retries_left -= 1
            time.sleep(delay_between_retries)

    status = c.getinfo(pycurl.HTTP_CODE)

    c.close()
    os.sync()

    if not redirect and status == 302:
        print_banner("Download Status: %d" % status)
        return False
    if not status == 200:
        print_banner("Download Status: %d" % status)
        return False

    return success


def version_tuple(v):
    return tuple(map(int, (v.split("."))))


def get_flutter_sdk_path() -> str:
    import subprocess
    (result, output) = subprocess.getstatusoutput('which flutter')
    if result:
        return ''

    return os.path.dirname(os.path.dirname(output.rstrip()))


def get_flutter_sdk_version() -> str:
    import json
    import subprocess

    (result, output) = subprocess.getstatusoutput('which flutter')
    if result:
        print_banner(f'failed {result} (cmd was which flutter)')
        return ''

    bin_path = os.path.dirname(output.rstrip())
    flutter_version_json = os.path.join(bin_path, 'cache', 'flutter.version.json')

    if not os.path.exists(flutter_version_json):
        print_banner(f'Missing {flutter_version_json}')
        return ''

    with open(os.path.join(os.path.dirname(flutter_version_json), flutter_version_json), encoding='utf-8') as f:
        flutter_version_json = json.load(f)

        if 'flutterVersion' not in flutter_version_json:
            print_banner(f'Missing key: flutterVersion in {flutter_version_json}')
            return ''

        flutter_version = flutter_version_json['flutterVersion']
        return flutter_version


def test_internet_connection() -> bool:
    """Test internet by connecting to nameserver"""
    import pycurl

    c = pycurl.Curl()
    c.setopt(pycurl.URL, "https://dns.google")
    c.setopt(pycurl.FOLLOWLOCATION, 0)
    c.setopt(pycurl.CONNECTTIMEOUT, 5)
    c.setopt(pycurl.NOSIGNAL, 1)
    c.setopt(pycurl.NOPROGRESS, 1)
    c.setopt(pycurl.NOBODY, 1)
    try:
        c.perform()
    except pycurl.error as e:
        error_code, message = e
        print(f'pycurl exception: {error_code}: {message}')
        pass

    res = False
    if c.getinfo(pycurl.RESPONSE_CODE) == 200:
        res = True

    return res
