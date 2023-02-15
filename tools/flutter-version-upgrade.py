#!/usr/bin/env python3

"""Script to update conf/include/engine-revision.json"""

import json
import os
import urllib.request
from typing import Dict

THIS_DIR = os.path.dirname(__file__)
RELEASE_JSON = os.path.abspath(os.path.join(
    THIS_DIR, '..', 'conf', 'include', 'releases_linux.json'))
ENGINE_JSON = os.path.abspath(os.path.join(
    THIS_DIR, '..', 'conf', 'include', 'engine-revision.json'))


def get_engine_commit(revision: str) -> str:
    """Get matching engine commit hash."""
    with urllib.request.urlopen(
            f'https://raw.githubusercontent.com/flutter/flutter/{revision}/bin/internal/engine.version') as f:
        return f.read().decode('utf-8').strip()


def create_engine_dict() -> Dict:
    """Create mapping of engine revisions to release names."""
    result = {}
    with open(RELEASE_JSON) as i:
        for release in json.load(i).get('releases', []):
            print(f'Getting engine commit for release {release["version"]}')
            result[release['version']] = get_engine_commit(release['hash'])
    return result


def get_release_dict() -> Dict:
    """Get latest the release json."""
    with urllib.request.urlopen(
            'https://storage.googleapis.com/flutter_infra_release/releases/releases_linux.json') as f:
        data = f.read()
        encoding = f.info().get_content_charset('utf-8')
        return json.loads(data.decode(encoding))


def main():
    """Bump all data."""
    releases = get_release_dict()
    with open(RELEASE_JSON, 'w') as o:
        json.dump(releases, o, sort_keys=True, indent=2)
    print('Updated releases')
    engine = create_engine_dict()
    with open(ENGINE_JSON, 'w') as o:
        json.dump(engine, o, sort_keys=True, indent=2)
    print('Updated engine revisions')


if __name__ == '__main__':
    main()
