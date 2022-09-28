FLUTTER_SDK_TAG ??= "3.3.1"

def get_flutter_archive(d):
    return _get_flutter_release_info(d, "archive")

def get_flutter_sha256(d):
    return _get_flutter_release_info(d, "sha256")

def get_flutter_hash(d):
    return _get_flutter_release_info(d, "hash")

def _get_flutter_release_info(d, key):
    import urllib
    import json
    ret = ""
    flutter_sdk_tag = d.getVar("FLUTTER_SDK_TAG")
    with urllib.request.urlopen('https://storage.googleapis.com/flutter_infra_release/releases/releases_linux.json') as f:
        releases_linux_json = json.load(f)
        if flutter_sdk_tag == "AUTOINC":
            flutter_sdk_tag = releases_linux_json["current_release"]["stable"]

        releases = releases_linux_json["releases"]
        for r in releases:
            if r["version"] == flutter_sdk_tag or r["hash"] == flutter_sdk_tag:
                ret = r[key]
                break

    if ret == "":
        raise ValueError("Could not get flutter sdk archive url")

    return ret

def get_flutter_sdk_version(d):
    import urllib
    import json
    ret = ""
    flutter_sdk_tag = d.getVar("FLUTTER_SDK_TAG")
    with urllib.request.urlopen('https://storage.googleapis.com/flutter_infra_release/releases/releases_linux.json') as f:
        releases_linux_json = json.load(f)
        if flutter_sdk_tag == "AUTOINC":
            flutter_sdk_tag = releases_linux_json["current_release"]["stable"]

        releases = releases_linux_json["releases"]
        for r in releases:
            if r["version"] == flutter_sdk_tag or r["hash"] == flutter_sdk_tag:
                ret = r["version"]
                break

    if ret == "":
        raise ValueError("Could not get flutter sdk archive url")

    return ret

# gn_get_engine_commit
#
# if repo is not default use FLUTTER_ENGINE_COMMIT
# otherwise use FLUTTER_SDK_TAG value
#
# You would need to set FLUTTER_SDK_TAG to match your custom repo
#
def gn_get_engine_commit(d):
    """ Sets FLUTTER_ENGINE_COMMIT variable """
    import urllib.request

    FLUTTER_ENGINE_VERSION_FMT = \
        'https://raw.githubusercontent.com/flutter/flutter/%s/bin/internal/engine.version'
    revision = get_flutter_hash(d)

    with urllib.request.urlopen(FLUTTER_ENGINE_VERSION_FMT % (revision)) as f:
        return f.read().decode('utf-8').strip()

    bb.fatal('Unable to get engine commit')
