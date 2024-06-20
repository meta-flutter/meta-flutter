#!/usr/bin/env python3

#
# Script that update Flutter revisions
# Reads and preserves the information in the original revision files 
# and merges it with the latest version information.
#
# Creates flutter revisions files:
#
#   dart-revision.json
#   engine-revision.json
#   releases_linux.json
#
# Run this script in meta-flutter/conf/include
#
# git status
# ../../generate_flutter_revision.py
# git diff
#

from collections import OrderedDict
import errno
import io
import json
import urllib.request

def download_flutter_release_info(url, local_path):
    try:
        # Open the URL and read the data
        with urllib.request.urlopen(url) as response:
            data = response.read()

        # Write the data to a local file
        with open(local_path, 'wb') as file:
            file.write(data)
        print(f"JSON file downloaded successfully and saved to {local_path}")
    except Exception as e:
        print(f"An error occurred: {e}")

def read_previous_release_info(dart_revision, engine_revision):
    data_revision = {}
    try:
        # Read the JSON file
        with open(dart_revision, 'r', encoding='utf-8') as file:
            releases = json.load(file)

        for key, value in releases.items():
            data_revision[key] = {"dart_sdk_version":value}
    except Exception as e:
        print(f"An error occurred while reading the previsou file: {e}")

    try:
        with open(engine_revision, 'r', encoding='utf-8') as file:
            releases = json.load(file)

        for key, value in releases.items():
            _revision = {}
            if key in data_revision and 'dart_sdk_version' in data_revision[key]:
                _revision = data_revision[key];
            _revision["engine_version"] = value;
            data_revision[key] = _revision;

    except Exception as e:
        print(f"An error occurred while reading the previsou file: {e}")

    return data_revision

def get_flutter_release_info(data_revision, local_path):
    try:
        # Read the JSON file
        with open(local_path, 'r', encoding='utf-8') as file:
            releases_linux_json = json.load(file)

        releases = releases_linux_json["releases"]
        for r in releases:
            engine_version = _get_engine_commit(r["hash"])
            data_revision[r["version"]] = {"engine_version":engine_version, \
                                           "dart_sdk_version":r["dart_sdk_version"]}
            print(f'{r["version"]}: ')
            print(f'\tengine_version: {engine_version}')
            print(f'\tdart_sdk_version: {r["dart_sdk_version"]}')

    except Exception as e:
        print(f"An error occurred while reading the file: {e}")
    return data_revision

def _get_engine_commit(revision):
    try:
        FLUTTER_ENGINE_VERSION_FMT = \
            'https://raw.githubusercontent.com/flutter/flutter/%s/bin/internal/engine.version'

        with urllib.request.urlopen(FLUTTER_ENGINE_VERSION_FMT % (revision)) as f:
            return f.read().decode('utf-8').strip()
    except Exception as e:
        print(f"An error occurred: {e}")
    return ''

def dump_json_to_file(data, dart_revision, engine_revision):
    try:
        # Sort data: sort by keys
        sorted_data = OrderedDict(sorted(data.items()))
        dart_data = {}
        engine_data = {}

        for key, value in sorted_data.items():
            if 'dart_sdk_version' in value:
                dart_data[key] = value['dart_sdk_version']
            if 'engine_version' in value:
                engine_data[key] = value['engine_version']

        # Save sorted data to a JSON file
        with open(dart_revision, 'w', encoding='utf-8') as file:
            json.dump(dart_data, file, ensure_ascii=True, indent=2)
            print(f"JSON data has been saved to {dart_revision}.")

        with open(engine_revision, 'w', encoding='utf-8') as file:
            json.dump(engine_data, file, ensure_ascii=True, indent=2)
            print(f"JSON data has been saved to {engine_revision}.")

    except Exception as e:
        print(f"An error occurred while saving the file: {e}")


def main():
    url = 'https://storage.googleapis.com/flutter_infra_release/releases/releases_linux.json'
    local_path = 'releases_linux.json'
    dart_revision_path = 'dart-revision.json'
    engine_revision_path = 'engine-revision.json'

    download_flutter_release_info(url, local_path)
    # To preserve old version information
    pre_revision = read_previous_release_info(dart_revision_path, engine_revision_path)

    revision = get_flutter_release_info(pre_revision, local_path)
    dump_json_to_file(revision, dart_revision_path, engine_revision_path)

if __name__ == "__main__":
    main()
