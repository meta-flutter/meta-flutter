#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

FLUTTER_SDK_TAG ??= "3.7.7"

def get_flutter_archive(d):
    return _get_flutter_release_info(d, "archive")

def get_flutter_sha256(d):
    return _get_flutter_release_info(d, "sha256")

def get_flutter_hash(d):
    return _get_flutter_release_info(d, "hash")

def _get_flutter_release_info(d, key):
    import json
    import inspect
    ret = ""
    flutter_sdk_tag = d.getVar("FLUTTER_SDK_TAG")
    cwd = os.path.dirname(inspect.getfile(lambda: None))
    with open(os.path.join(cwd, 'releases_linux.json'), encoding='utf-8') as f:
        releases_linux_json = json.load(f)

        if flutter_sdk_tag == "AUTOINC":
            flutter_sdk_tag = releases_linux_json["current_release"]["stable"]

        for r in releases_linux_json["releases"]:
            if r["version"] == flutter_sdk_tag or r["hash"] == flutter_sdk_tag:
                ret = r[key]
                break

    if ret == "":
        raise ValueError("Could not get flutter sdk archive url")

    bb.debug(1, f"Choosing {ret} for requested {key}")
    return ret

def get_flutter_sdk_version(d):
    return _get_flutter_release_info(d, "version")

# gn_get_engine_commit
#
# if repo is not default use FLUTTER_ENGINE_COMMIT
# otherwise use FLUTTER_SDK_TAG value
#
# You would need to set FLUTTER_SDK_TAG to match your custom repo
#
def gn_get_engine_commit(d):
    """ Sets FLUTTER_ENGINE_COMMIT variable """
    import json
    import inspect
    ret = ""
    flutter_tag = d.getVar("FLUTTER_SDK_TAG")
    cwd = os.path.dirname(inspect.getfile(lambda: None))
    with open(os.path.join(cwd, 'engine-revision.json'), encoding='utf-8') as f:
        engine_json = json.load(f)
        ret = engine_json.get(flutter_tag, '')

    if ret == "":
        raise ValueError("Unable to get engine commit")

    bb.debug(1, f"Engine commit for {flutter_tag} is {ret}")
    return ret
