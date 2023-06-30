#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Embedded Linux embedding for Flutter"
AUTHOR = "Sony Group Corporation"
HOMEPAGE = "https://github.com/sony/flutter-embedded-linux"
BUGTRACKER = "https://github.com/sony/flutter-embedded-linux/issues"
SECTION = "graphics"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d45359c88eb146940e4bede4f08c821a"

DEPENDS += "\
    flutter-engine \
    libinput \
    libxkbcommon \
    systemd \
    virtual/egl \
    "

RDEPENDS:${PN} += "xkeyboard-config"

REQUIRED_DISTRO_FEATURES += "opengl systemd"

SRC_URI = "git://github.com/sony/flutter-embedded-linux.git;protocol=https;branch=master"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

inherit pkgconfig cmake features_check

require conf/include/flutter-version.inc

EXTRA_OECMAKE += "-D USER_PROJECT_PATH=${S}/examples/${PN}"

do_configure:prepend() {

    rm -rf ${S}/build || true

    mkdir -p ${S}/build || true

    # determine build type based on what flutter-engine installed
    FLUTTER_RUNTIME_MODES="$(ls ${STAGING_DIR_TARGET}${datadir}/flutter/${FLUTTER_SDK_VERSION}/)"

    for FLUTTER_RUNTIME_MODE in $FLUTTER_RUNTIME_MODES; do
        if [ "${FLUTTER_RUNTIME_MODE}" = "release" ]; then
            break
        elif [ "${FLUTTER_RUNTIME_MODE}" = "jit_release" ]; then
            break
        elif [ "${FLUTTER_RUNTIME_MODE}" = "profile" ]; then
            break
        elif [ "${FLUTTER_RUNTIME_MODE}" = "debug" ]; then
            break
        fi
    done

    FLUTTER_ENGINE_PATH=${STAGING_DIR_TARGET}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}

    ln -sf ${FLUTTER_ENGINE_PATH}/lib/libflutter_engine.so ${S}/build/libflutter_engine.so
}

do_package_qa[noexec] = "1"
EXCLUDE_FROM_SHLIBS = "1"

RDEPENDS:${PN} += "flutter-engine"

python () {
    d.setVar('FLUTTER_SDK_VERSION', get_flutter_sdk_version(d))
}
