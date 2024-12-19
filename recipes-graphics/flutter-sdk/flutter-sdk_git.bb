#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
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

S = "${WORKDIR}/flutter"

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
    cmd = re.sub('\s{2,}', ' ', cmd)

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
    workdir = d.getVar('WORKDIR')

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

python () {
    d.setVar('FLUTTER_SDK_VERSION', get_flutter_sdk_version(d))
}

ALLOW_EMPTY:${PN} = "1"

FILES:${PN} = "${datadir}/flutter/sdk"

INSANE_SKIP:${PN} += "already-stripped file-rdeps"

BBCLASSEXTEND = "native nativesdk"
