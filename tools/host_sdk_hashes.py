#!/usr/bin/env python3
#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
"""Record the sha256 of Flutter's host SDK archives in conf/include/host-sdk.json.

Flutter publishes one Linux SDK archive and it is x86-64, so a build host of
another architecture needs the dart-sdk and engine artifacts Flutter publishes
per engine revision. Those have no published checksum and SRC_URI over https
requires one, so they are hashed once and recorded here. See #1233.

  tools/host_sdk_hashes.py --path .                  # the pinned version
  tools/host_sdk_hashes.py --path . --arch arm64
  tools/host_sdk_hashes.py --path . --version 3.47.7

The roll runs this, so a roll leaves the record current. It downloads about
260MB per architecture and skips an architecture already recorded.
"""

import argparse
import hashlib
import json
import os
import re
import sys
import urllib.request

BASE_URL = 'https://storage.googleapis.com/flutter_infra_release/flutter'

# Keyed as the recipe reads them. The dart-sdk zip carries its own dart-sdk/
# directory; the other two are flat and unpack into the host's artifacts dir.
ARCHIVES = {
    'dart-sdk': 'dart-sdk-linux-{arch}.zip',
    'artifacts': 'linux-{arch}/artifacts.zip',
    'font-subset': 'linux-{arch}/font-subset.zip',
}

ARCHES = ('arm64',)


def pinned_version(include_path):
    """FLUTTER_SDK_TAG from flutter-version.inc."""
    with open(os.path.join(include_path, 'flutter-version.inc')) as f:
        match = re.search(r'^FLUTTER_SDK_TAG \?\?= "([^"]+)"', f.read(), re.M)
    if not match:
        sys.exit('No FLUTTER_SDK_TAG in flutter-version.inc')
    return match.group(1)


def engine_revision(include_path, version):
    with open(os.path.join(include_path, 'engine-revision.json')) as f:
        revision = json.load(f).get(version)
    if not revision:
        sys.exit(f'No engine revision for {version} in engine-revision.json')
    return revision


def sha256_of(url):
    """Stream the archive and hash it; these run to a quarter of a gigabyte."""
    digest = hashlib.sha256()
    with urllib.request.urlopen(url) as response:
        if response.status != 200:
            sys.exit(f'{url} returned {response.status}')
        while True:
            chunk = response.read(1 << 20)
            if not chunk:
                break
            digest.update(chunk)
    return digest.hexdigest()


def update(root_path, version=None, arches=ARCHES, force=False):
    include_path = os.path.join(root_path, 'conf', 'include')
    version = version or pinned_version(include_path)
    revision = engine_revision(include_path, version)

    record_path = os.path.join(include_path, 'host-sdk.json')
    record = {}
    if os.path.exists(record_path):
        with open(record_path) as f:
            record = json.load(f)

    changed = False
    for arch in arches:
        have = record.get(revision, {}).get(arch, {})
        if not force and all(name in have for name in ARCHIVES):
            print(f'{arch} already recorded for {revision}')
            continue
        hashes = {}
        for name, template in ARCHIVES.items():
            url = f'{BASE_URL}/{revision}/{template.format(arch=arch)}'
            print(f'Hashing {url}')
            hashes[name] = sha256_of(url)
        record.setdefault(revision, {})[arch] = hashes
        changed = True

    if not changed:
        return False

    with open(record_path, 'w') as f:
        json.dump(record, f, indent=2, sort_keys=True)
        f.write('\n')
    print(f'Wrote {record_path} for {version} at engine {revision}')
    return True


def main():
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument('--path', default='.', help='layer root')
    parser.add_argument('--version', help='Flutter version (default: the pinned one)')
    parser.add_argument('--arch', action='append', choices=list(ARCHES),
                        help='build host architecture, repeatable')
    parser.add_argument('--force', action='store_true',
                        help='re-hash an architecture already recorded')
    args = parser.parse_args()
    update(args.path, args.version, tuple(args.arch) if args.arch else ARCHES,
           args.force)


if __name__ == '__main__':
    main()
