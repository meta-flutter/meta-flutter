#!/usr/bin/env python3
# SPDX-FileCopyrightText: (C) 2020-2024 Joel Winarske
# SPDX-License-Identifier: MIT

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
    cmd = re.sub(r'\s{2,}', ' ', cmd)
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


# Phrases that identify a license, matched against the file's text with
# whitespace collapsed. Titles are spelled without the comma that the
# cross-references use ("Apache License Version 2.0" as a heading, versus
# "the GNU Lesser General Public License, Version 2.1" where MPL-2.0 names its
# secondary licenses) so that naming a license does not read as being it.
_LICENSE_MARKERS = [
    ('Apache-2.0',   (('apache license version 2.0',), ())),
    ('GPL-3.0',      (('gnu general public license version 3',), ())),
    ('GPL-2.0',      (('gnu general public license version 2',), ())),
    ('LGPL-3.0',     (('gnu lesser general public license version 3',), ())),
    ('LGPL-2.1',     (('gnu lesser general public license version 2.1',), ())),
    ('MPL-2.0',      (('mozilla public license version 2.0',), ())),
    # BSD-3-Clause is BSD-2-Clause plus non-endorsement, so the two-clause form
    # is only itself when the third clause is absent.
    ('BSD-3-Clause', (('redistribution and use in source and binary forms',
                       'neither the name'), ())),
    ('BSD-2-Clause', (('redistribution and use in source and binary forms',),
                      ('neither the name',))),
    # The SIL Open Font License opens with the same sentence as MIT but grants
    # over "the Font Software"; flutter/games ships both, so MIT has to key on
    # its own object to avoid claiming every font license as MIT.
    ('OFL-1.1',      (('sil open font license',), ())),
    ('MIT',          (('free of charge, to any person obtaining a copy of this software',), ())),
    ('ISC',          (('permission to use, copy, modify, and/or distribute this software',), ())),
]

# Families where the license file alone cannot settle the identifier. A project
# shipping the GPL ships the same COPYING whether it is "version 3 only" or
# "version 3 or later" -- that distinction lives in the per-file headers, and
# the license text's own appendix always shows the or-later wording. So detect
# the family and accept either variant rather than guess.
_LICENSE_FAMILIES = {
    'GPL-3.0':  ('GPL-3.0-only', 'GPL-3.0-or-later'),
    'GPL-2.0':  ('GPL-2.0-only', 'GPL-2.0-or-later'),
    'LGPL-3.0': ('LGPL-3.0-only', 'LGPL-3.0-or-later'),
    'LGPL-2.1': ('LGPL-2.1-only', 'LGPL-2.1-or-later'),
}


def detect_licenses(license_path: str) -> list:
    """Return the SPDX identifiers a license file's text contains.

    A list, because upstreams routinely ship one file holding several licenses
    -- flutter/games concatenates the Chromium BSD-3-Clause notice and the full
    Apache-2.0 text, and a recipe claiming either one alone would be wrong.

    Detection is deliberately conservative: a file matching nothing yields an
    empty list, so callers can tell "disagrees with the source" from "could not
    be checked".
    """
    try:
        with open(license_path, 'r', encoding='utf-8', errors='replace') as f:
            text = f.read().lower()
    except OSError:
        return []

    # Collapse whitespace so matching survives the comment prefixes and hard
    # wrapping that upstreams wrap their license text in.
    text = ' '.join(text.split())

    found = []
    for spdx, (required, forbidden) in _LICENSE_MARKERS:
        if all(m in text for m in required) and not any(m in text for m in forbidden):
            found.append(spdx)
    return found


# Multiple licenses are joined with "AND" on master and with "&" on the release
# branches: oe-core 51c7930220 made AND the native SPDX operator, and before it
# a bare AND parsed as a license *name*. Both spellings are correct on their own
# branch and each is a regression on the other, so the value is declared here
# and checked, rather than left to a sweep to notice after the fact. Change it
# in the same commit that branches for a new release. See README.
LICENSE_OPERATOR = 'AND'
_LICENSE_OPERATOR_OTHER = '&'


def detect_license(license_path: str) -> str:
    """detect_licenses() as an SPDX expression, e.g. "BSD-3-Clause AND MIT"."""
    return f' {LICENSE_OPERATOR} '.join(sorted(detect_licenses(license_path)))


def wrong_license_operator(declared: str) -> str:
    """The operator in [declared] that this branch does not use, else ''.

    Does not require an operator to be present -- a single-license value has
    none. "&" cannot occur inside an SPDX id, so a substring test is enough for
    it; "AND" can, so that one is matched on token boundaries.
    """
    if not declared:
        return ''
    if LICENSE_OPERATOR == 'AND':
        return '&' if '&' in declared else ''
    return 'AND' if 'AND' in re.split(r'[\s()]+', declared) else ''
    # Match on token boundaries: "&" cannot appear inside an SPDX id, but "AND"
    # would otherwise match inside a name that contains it.
    tokens = re.split(r'[\s()]+', declared)
    if _LICENSE_OPERATOR_OTHER in tokens:
        return _LICENSE_OPERATOR_OTHER
    if LICENSE_OPERATOR == 'AND' and '&' in declared:
        return '&'
    if LICENSE_OPERATOR == '&' and 'AND' in tokens:
        return 'AND'
    return ''


def license_agrees_with_source(declared: str, detected_list: list) -> bool:
    """Whether a declared LICENSE value is consistent with the detected text.

    Detection is coarse on purpose. A great many licenses embed the BSD or MIT
    wording verbatim and then add a clause -- Sendmail, OpenSSL, ZPL and the
    whole X11 family all read as BSD or MIT to a text match. So a declared
    identifier counts as agreeing when it is the detected one, a variant of a
    family the text cannot disambiguate (see _LICENSE_FAMILIES), or a more
    specific spelling of it (BSD-3-Clause-Clear over BSD-3-Clause).

    What this still catches is the case worth catching: a declared license with
    no relation at all to the shipped text, which is how depot-tools carried
    LICENSE = "GPLv3" over BSD-3-Clause.
    """
    if not detected_list:
        return True          # nothing to contradict it

    declared_set = {t for t in declared.replace('(', ' ').replace(')', ' ').split()
                    if t.upper() not in ('AND', 'OR', '&', '|')}

    for want in detected_list:
        variants = {want} | set(_LICENSE_FAMILIES.get(want, ()))
        if declared_set & variants:
            continue
        # A more specific spelling of the detected license, or a derivative
        # that embeds its text.
        if any(d.startswith(want + '-') or want.startswith(d + '-') for d in declared_set):
            continue
        return False
    return True
