#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "Embedded Linux embedding for Flutter"
HOMEPAGE = "https://github.com/flutter-elinux/flutter-embedded-linux"
BUGTRACKER = "https://github.com/flutter-elinux/flutter-embedded-linux/issues"
SECTION = "graphics"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d45359c88eb146940e4bede4f08c821a"

DEPENDS += "\
    flutter-engine \
    libinput \
    libxkbcommon \
    virtual/egl \
    "

RDEPENDS:${PN} += " \
    xkeyboard-config \
    libxkbcommon \
    ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '', 'libuv', d)} \
    "

REQUIRED_DISTRO_FEATURES += "opengl"

SRC_REPO ??= "github.com/flutter-elinux/flutter-embedded-linux.git"
SRC_REPO_BRANCH ??= "master"

SRC_URI = "git://${SRC_REPO};protocol=https;branch=${SRC_REPO_BRANCH}"
SRCREV ??= "ee831b1510e16911ca81200a3f066daaf2a087af"

inherit pkgconfig cmake features_check

require conf/include/flutter-version.inc

PACKAGECONFIG:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', 'libuv', d)}"
PACKAGECONFIG[systemd] = ",,systemd,libuv"
PACKAGECONFIG[libuv] = ",,libuv"

EXTRA_OECMAKE += "-D USER_PROJECT_PATH=${S}/examples/${PN}"

do_configure:prepend() {

    rm -rf ${S}/build || true

    mkdir -p ${S}/build || true

    for FLUTTER_ENGINE in ${STAGING_DIR_TARGET}${datadir}/flutter/${FLUTTER_SDK_VERSION}/*/lib/libflutter_engine.so; do
        [ -e "$FLUTTER_ENGINE" ] || continue
        ln -sf "$FLUTTER_ENGINE" ${S}/build/libflutter_engine.so
        break
    done

    [ -e ${S}/build/libflutter_engine.so ] || bbfatal "No libflutter_engine.so found in the target sysroot"
}

PRIVATE_LIBS:${PN} = "libflutter_engine.so"

RDEPENDS:${PN} += "flutter-engine"

python () {
    d.setVar('FLUTTER_SDK_VERSION', get_flutter_sdk_version(d))
}
