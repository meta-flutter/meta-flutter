#
# Copyright (c) 2020-2024 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "Flutter makes it easy and fast to build beautiful apps for mobile and beyond."
DESCRIPTION = "Flutter is Google's SDK for crafting beautiful, fast user experiences for \
               mobile, web, and desktop from a single codebase. Flutter works with \
               existing code, is used by developers and organizations around the world, \
               and is free and open source."
AUTHOR = "Google"
HOMEPAGE = "https://flutter.dev/"
BUGTRACKER = "https://github.com/flutter/flutter/issues"
SECTION = "graphics"
CVE_PRODUCT = ""

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1d84cf16c48e571923f837136633a265"

DEPENDS += "\
    ca-certificates-native \
    curl-native \
    ninja-native \
    unzip-native \
    "

RDEPENDS:${PN} += "\
    atk \
    ca-certificates \
    curl \
    fontconfig \
    gtk+3 \
    pango \
    perl \
    perl-modules \
    unzip \
    "

require conf/include/flutter-version.inc

PV = "${FLUTTER_SDK_VERSION}"

inherit pkgconfig

SRC_URI = "\
    https://storage.googleapis.com/flutter_infra_release/releases/${@get_flutter_archive(d)};name=flutter-sdk \
    https://storage.googleapis.com/flutter_infra_release/flutter/fonts/3012db47f3130e62f7cc0beabff968a33cbec8d8/fonts.zip;name=fonts;destsuffix=${D}${datadir}/flutter/sdk/bin/cache/artifacts/material_fonts \
"
SRC_URI[flutter-sdk.sha256sum] = "${@get_flutter_sha256(d)}"
SRC_URI[fonts.sha256sum] = "e56fa8e9bb4589fde964be3de451f3e5b251e4a1eafb1dc98d94add034dd5a86"

S = "${UNPACKDIR}/flutter"

def getstatusoutput(cmd, cwd, env):
    from subprocess import check_output, CalledProcessError, STDOUT

    try:
        data = check_output(cmd, shell=True, universal_newlines=True, stderr=STDOUT, cwd=cwd, env=env)
        status = 0
    except CalledProcessError as ex:
        data = ex.output
        status = ex.returncode
    if data[-1:] == '\n':
        data = data[:-1]
    return status, data

def run_command(d, cmd, cwd, env):
    import subprocess
    import re

    # replace all consecutive whitespace characters (tabs, newlines etc.) with a single space
    cmd = re.sub(r'\s{2,}', ' ', cmd)

    bb.note('Running [%s] in %s' % (cmd, cwd))
    (retval, output) = getstatusoutput(cmd, cwd, env)
    if retval:
        bb.error("failed %s (cmd was %s)%s" % (retval, cmd, ":\n%s" % output if output else ""))
        return

    bb.note(f'{output}')


do_unpack[network] = "1"
do_unpack[depends] += " \
    ca-certificates-native:do_populate_sysroot \
    curl-native:do_populate_sysroot \
    ninja-native:do_populate_sysroot \
    unzip-native:do_populate_sysroot \
"
python do_unpack:append() {
    import shutil

    # clean cache folder if it exists
    source_dir = d.getVar('S')
    shutil.rmtree(f'{source_dir}/bin/cache', ignore_errors=True)

    env = os.environ

    staging_dir_native = d.getVar('STAGING_DIR_NATIVE')
    env['CURL_CA_BUNDLE'] = f'{staging_dir_native}/etc/ssl/certs/ca-certificates.crt'

    path = env['PATH']
    env['PATH']           = f'{source_dir}/bin:{path}'
    env['PUB_CACHE']      = f'{source_dir}/.pub-cache'

    workdir = d.getVar('WORKDIR')
    # required for dart: https://github.com/dart-lang/sdk/issues/41560
    env['HOME'] = f'{workdir}'
    # required for flutter: https://github.com/flutter/flutter/issues/59430
    env['XDG_CONFIG_HOME'] = f'{workdir}'

    http_proxy = d.getVar('http_proxy')
    if http_proxy != None:
        env['http_proxy']     = f'{http_proxy}'

    https_proxy = d.getVar('https_proxy')
    if https_proxy != None:
        env['https_proxy']    = f'{https_proxy}'

    http_proxy_ = d.getVar('HTTP_PROXY')
    if http_proxy_ != None:
        env['HTTP_PROXY']     = f'{http_proxy_}'

    https_proxy_ = d.getVar('HTTPS_PROXY')
    if https_proxy_ != None:
        env['HTTPS_PROXY']    = f'{https_proxy_}'

    env['NO_PROXY']       = 'localhost,127.0.0.1,::1'

    flutter_sdk_tag = d.getVar('FLUTTER_SDK_TAG')
    bb.note(f'Flutter SDK: {flutter_sdk_tag}')

    run_command(d, 'flutter config --clear-features', source_dir, env)
    run_command(d, 'flutter config --enable-linux-desktop', source_dir, env)
    run_command(d, 'flutter config --enable-custom-devices', source_dir, env)
    run_command(d, 'flutter config --enable-web', source_dir, env)
    run_command(d, 'flutter config --no-analytics', source_dir, env)
    run_command(d, 'dart --disable-analytics', source_dir, env)
    run_command(d, 'flutter config --list', source_dir, env)

    # check your installation and build the initial snapshot of the `flutter` tool
    run_command(d, 'flutter doctor -v', source_dir, env)
    
    # download all of the pub package dependencies needed to build any of the packages in the Flutter main distribution
    run_command(d, 'flutter update-packages', source_dir, env)

    # cache template packages
    tmp_path = os.path.join(workdir, 'tmp')
    run_command(d, f'mkdir -p {tmp_path}', source_dir, env)
    run_command(d, 'flutter create --template=app app_sample', tmp_path, env)
    run_command(d, 'flutter create --template=package package_sample', tmp_path, env)
    run_command(d, 'flutter create --template=plugin plugin_sample', tmp_path, env)
    run_command(d, f'rm -rf {tmp_path}', source_dir, env)
}

do_install() {

    chmod a+rw ${S} -R

    install -d ${D}${datadir}/flutter/sdk

    cp -rTv ${S}/. ${D}${datadir}/flutter/sdk
}

do_install:append:class-target () {
    rm -rf ${D}${datadir}/flutter/sdk/bin/cache/artifacts/engine
    rm -rf ${D}${datadir}/flutter/sdk/bin/cache/dart-sdk/bin
}

# pub records where it resolved a package, and it records it absolutely: every
# package_config.json in the SDK names its dependencies as file:// URIs under
# ${S}, which is inside TMPDIR. Nothing recreates that path in a recipe that
# consumes the SDK -- there it lives under recipe-sysroot-native -- so all of
# those entries dangle, and the flutter tool concludes flutter_tools is
# unresolved and re-runs `dart pub get` on it. That resolve is not offline. It
# reaches pub.dev on every app build, and in a task with no network it does not
# fail, it hangs until something kills it.
#
# The URIs may be relative to the file that holds them, which flutter_tools'
# own entry ("../") already is. Making the rest relative makes the staged SDK
# relocatable, and takes an absolute TMPDIR out of a staged artifact while it
# is there. See #566.
python relativize_dart_package_configs() {
    import json
    import os
    import time
    import urllib.parse

    s_root = os.path.realpath(d.getVar('S'))
    sdk_root = d.getVar('D') + d.getVar('datadir') + '/flutter/sdk'

    for root, dirs, files in os.walk(sdk_root):
        if 'package_config.json' not in files:
            continue
        path = os.path.join(root, 'package_config.json')
        with open(path, 'r') as f:
            cfg = json.load(f)

        rewritten = 0
        foreign = []
        for pkg in cfg.get('packages', []):
            uri = pkg.get('rootUri', '')
            if not uri.startswith('file://'):
                continue
            abs_path = urllib.parse.unquote(urllib.parse.urlparse(uri).path)
            if abs_path != s_root and not abs_path.startswith(s_root + os.sep):
                # Outside the SDK entirely: relativizing would not help and
                # would hide it, so say so and leave it.
                foreign.append('%s -> %s' % (pkg.get('name'), uri))
                continue
            staged = os.path.join(sdk_root, os.path.relpath(abs_path, s_root))
            pkg['rootUri'] = os.path.relpath(staged, root)
            rewritten += 1

        if foreign:
            bb.warn('%s: %d package(s) resolved outside the SDK, left absolute: %s'
                    % (os.path.relpath(path, sdk_root), len(foreign),
                       ', '.join(foreign[:3])))
        if rewritten:
            with open(path, 'w') as f:
                json.dump(cfg, f, indent=2)
            bb.note('%s: made %d rootUri entries relative'
                    % (os.path.relpath(path, sdk_root), rewritten))

        # Make the resolution unambiguously newer than what it resolves.
        #
        # The flutter tool re-runs `pub get` on packages/flutter_tools when its
        # package_config.json does not look newer than the pubspec beside it --
        # and that resolve carries no --offline, so in a task with no network it
        # hangs until something kills it. Relative rootUris (above) stopped the
        # paths dangling; they do not say anything about freshness.
        #
        # do_install copies with `cp -rT`, which stamps mtimes at copy time in
        # traversal order, so which of the two ends up newer is not decided by
        # anything. sstate then preserves whatever it got, which is why the same
        # SDK can build clean once and hang the next time it is restored.
        #
        # Stamp it here instead, after the file is written, and let sstate carry
        # that.
        stamp = time.time()
        for near in (os.path.join(root, os.pardir, 'pubspec.yaml'),
                     os.path.join(root, os.pardir, 'pubspec.lock')):
            if os.path.isfile(near):
                os.utime(near, (stamp - 2, stamp - 2))
        os.utime(path, (stamp, stamp))
        graph = os.path.join(root, 'package_graph.json')
        if os.path.isfile(graph):
            os.utime(graph, (stamp, stamp))
}
do_install[postfuncs] += "relativize_dart_package_configs"

python () {
    d.setVar('FLUTTER_SDK_VERSION', get_flutter_sdk_version(d))
}

ALLOW_EMPTY:${PN} = "1"

FILES:${PN} = "${datadir}/flutter/sdk"

INSANE_SKIP:${PN} += "already-stripped file-rdeps libdir"
INSANE_SKIP:${PN}-dbg += "libdir"
INSANE_SKIP:class-nativesdk += "buildpaths"

BBCLASSEXTEND = "native nativesdk"
