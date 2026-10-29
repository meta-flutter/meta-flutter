#
# Copyright (c) 2020-2024 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

FLUTTER_SDK_TAG ??= "3.47.9"

# The package that ships libflutter_engine.so.
#
# flutter-engine is ALLOW_EMPTY and carries no files of its own: the per-mode
# packages claim them, and it only RDEPENDs on whichever modes were built.
# file-rdeps resolves a shared library against the packages named in a recipe's
# RDEPENDS and does not follow a metapackage's own, so a recipe that links the
# engine has to name the package that carries it. Depending on flutter-engine
# alone gives "requires libflutter_engine.so(), but no providers found in
# RDEPENDS" -- see #887.
#
# release is in the engine's default PACKAGECONFIG. Override in local.conf for
# an image that ships a different mode:
#
#   FLUTTER_ENGINE_PACKAGE = "flutter-engine-debug"
#
FLUTTER_ENGINE_PACKAGE ??= "flutter-engine-release"

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

# Flutter ships one Linux SDK archive and it is x86-64: every entry in
# releases_linux.json is dart_sdk_arch x64. Its bin/cache/dart-sdk and
# bin/cache/artifacts/engine/linux-x64 are x86-64 binaries, and do_unpack runs
# them, so on any other build host they have to be replaced with the archives
# Flutter publishes per engine revision. That is what the SDK's own
# bin/internal/update_dart_sdk.sh does from uname -m, and what this layer did
# until do_unpack stopped deleting bin/cache. See #1233.
FLUTTER_HOST_SDK_BASE_URL ??= "https://storage.googleapis.com/flutter_infra_release/flutter"

def flutter_build_host_arch(d):
    """Flutter's name for the build host's architecture."""
    build_arch = d.getVar("BUILD_ARCH")
    arch = {"x86_64": "x64", "aarch64": "arm64"}.get(build_arch)
    if arch is None:
        raise ValueError(
            f"No Flutter host SDK for build host {build_arch}: Flutter "
            "publishes dart-sdk-linux-x64 and dart-sdk-linux-arm64 only")
    return arch

def get_flutter_engine_revision(d):
    """The engine revision the pinned SDK was built from."""
    import json
    import inspect
    flutter_sdk_tag = d.getVar("FLUTTER_SDK_TAG")
    cwd = os.path.dirname(inspect.getfile(lambda: None))
    with open(os.path.join(cwd, 'engine-revision.json'), encoding='utf-8') as f:
        revision = json.load(f).get(flutter_sdk_tag, "")

    if revision == "":
        raise ValueError(
            f"No engine revision for {flutter_sdk_tag} in engine-revision.json")

    return revision

def get_flutter_host_sdk_hashes(d, arch):
    """sha256 of the host SDK archives for this engine revision and arch.

    The archives carry no published checksum, so they are recorded rather
    than fetched. tools/host_sdk_hashes.py writes them and the roll runs it.
    """
    import json
    import inspect
    revision = get_flutter_engine_revision(d)
    cwd = os.path.dirname(inspect.getfile(lambda: None))
    with open(os.path.join(cwd, 'host-sdk.json'), encoding='utf-8') as f:
        hashes = json.load(f).get(revision, {}).get(arch, {})

    missing = [k for k in ("dart-sdk", "artifacts", "font-subset")
               if k not in hashes]
    if missing:
        raise ValueError(
            f"conf/include/host-sdk.json has no {', '.join(missing)} sha256 "
            f"for {arch} at engine {revision}. Record them with:\n"
            f"  tools/host_sdk_hashes.py --path . --arch {arch}")

    return hashes
