#!/usr/bin/env python3
#
# SPDX-FileCopyrightText: (C) 2020-2023 Joel Winarske
#
# SPDX-License-Identifier: Apache-2.0
#
#

import errno
import os
import sys

from sys import stderr as stream

# use kiB's
kb = 1024


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


def run_command(cmd: str, cwd: str) -> str:
    """ Run Command in specified working directory """
    import re
    import subprocess

    # replace all consecutive whitespace characters (tabs, newlines etc.) with a single space
    cmd = re.sub('\s{2,}', ' ', cmd)

    print('Running [%s] in %s' % (cmd, cwd))
    (retval, output) = subprocess.getstatusoutput(f'cd {cwd} && {cmd}')
    if retval:
        sys.exit("failed %s (cmd was %s)%s" % (retval, cmd, ":\n%s" % output if output else ""))

    print(output.rstrip())
    return output.rstrip()


def make_sure_path_exists(path: str):
    try:
        os.makedirs(path)
    except OSError as exception:
        if exception.errno != errno.EEXIST:
            raise


def get_md5sum(file: str) -> str:
    """Return md5sum of specified file"""
    import hashlib

    if not os.path.exists(file):
        return ''

    md5_hash = hashlib.md5()
    with open(file, "rb") as f:
        # Read and update hash in chunks of 4K
        for byte_block in iter(lambda: f.read(4096), b""):
            md5_hash.update(byte_block)

    return md5_hash.hexdigest()


def get_sha1sum(file: str) -> str:
    """Return sha1sum of specified file"""
    import hashlib

    if not os.path.exists(file):
        return ''

    sha1_hash = hashlib.sha1()
    with open(file, "rb") as f:
        # Read and update hash in chunks of 4K
        for byte_block in iter(lambda: f.read(4096), b""):
            sha1_hash.update(byte_block)

    return sha1_hash.hexdigest()


def get_sha256sum(file: str):
    """Return sha256sum of specified file"""
    import hashlib

    if not os.path.exists(file):
        return ''

    sha256_hash = hashlib.sha256()
    with open(file, "rb") as f:
        # Read and update hash in chunks of 4K
        for byte_block in iter(lambda: f.read(4096), b""):
            sha256_hash.update(byte_block)

    return sha256_hash.hexdigest()


def download_https_file(cwd, url, file, cookie_file, netrc, md5, sha1, sha256):
    download_filepath = os.path.join(cwd, file)

    sha256_file = os.path.join(cwd, file + '.sha256')
    if compare_sha256(download_filepath, sha256_file):
        print("%s exists, skipping download" % download_filepath)
        return True

    if os.path.exists(download_filepath):
        if md5:
            # don't download if md5 is good
            if md5 == get_md5sum(download_filepath):
                print("** Using %s" % download_filepath)
                return True
            else:
                os.remove(download_filepath)
        elif sha1:
            # don't download if sha1 is good
            if sha1 == get_sha1sum(download_filepath):
                print("** Using %s" % download_filepath)
                return True
            else:
                os.remove(download_filepath)
        elif sha256:
            # don't download if sha256 is good
            if sha256 == get_sha256sum(download_filepath):
                print("** Using %s" % download_filepath)
                return True
            else:
                os.remove(download_filepath)

    print("** Downloading %s via %s" % (file, url))
    res = fetch_https_binary_file(
        url, download_filepath, False, None, cookie_file, netrc)
    if not res:
        os.remove(download_filepath)
        print_banner("Failed to download %s" % file)
        return False

    if os.path.exists(download_filepath):
        if md5:
            expected_md5 = get_md5sum(download_filepath)
            if md5 != expected_md5:
                sys.exit('Download artifact %s md5: %s does not match expected: %s' %
                         (download_filepath, md5, expected_md5))
        elif sha1:
            expected_sha1 = get_sha1sum(download_filepath)
            if sha1 != expected_sha1:
                sys.exit('Download artifact %s sha1: %s does not match expected: %s' %
                         (download_filepath, md5, expected_sha1))
        elif sha256:
            expected_sha256 = get_sha256sum(download_filepath)
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
        f.write(sha256_val)


def fetch_https_progress(download_t, download_d, _upload_t, _upload_d):
    """callback function for pycurl.XFERINFOFUNCTION"""
    stream.write('Progress: {}/{} kiB ({}%)\r'.format(str(int(download_d / kb)), str(int(download_t / kb)),
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
                c.setopt(pycurl.WRITEFUNCTION, f.write)
                c.perform()

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
    except:
        pass

    res = False
    if c.getinfo(pycurl.RESPONSE_CODE) == 200:
        res = True

    return res
