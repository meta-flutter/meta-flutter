#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#
# Helper class for building Flutter Applications.
# Assumes that:
# - S is defined and points to source directory.
# - PUBSPEC_APPNAME is defined correctly.  This is the name value from pubspec.yml.
#

require conf/include/flutter-version.inc
require conf/include/app-utils.inc
require conf/include/common.inc
require conf/include/gn-utils.inc

DEPENDS += " \
    flutter-engine \
    "

BUILD_TYPE = "app"

# Force the Flutter build to target the cross-compile machine architecture
# rather than the build host (flutter defaults to `uname -m`). Use the
# target-arch mapping (aarch64 -> arm64, x86-64 -> x64), not clang_build_arch()
# which reports BUILD_ARCH. Use :append (not +=) so the ??= default survives.
FLUTTER_BUILD_ARGS ??= "bundle"
FLUTTER_BUILD_ARGS:append = " --target-platform linux-${@gn_target_arch_name(d)}"

# flutter build bundle only accepts 64-bit Linux target platforms. As of 3.47.1
# TargetPlatform carries linux_x64, linux_arm64 and linux_riscv64 and nothing
# 32-bit, so linux-arm and linux-x86 are rejected outright:
#
#   "linux-arm" is not an allowed value for option "--target-platform".
#
# This is a tooling limit, not an engine one. flutter-engine still builds for
# armv7 and ships a working 32-bit libflutter_engine.so, and the embedders link
# against it, so a 32-bit target remains usable -- it just cannot have its app
# bundle produced by the flutter tool. Skip the app recipes there rather than
# fail in do_compile after a full fetch and pub get.
FLUTTER_APP_SUPPORTED_ARCHS ?= "x64 arm64 riscv64"

python () {
    arch = d.getVar('GN_TARGET_ARCH_NAME')
    if not arch:
        return
    supported = (d.getVar('FLUTTER_APP_SUPPORTED_ARCHS') or '').split()
    if arch not in supported:
        raise bb.parse.SkipRecipe(
            "flutter build bundle has no linux-%s target platform; "
            "Flutter supports only %s for Linux app bundles" %
            (arch, ', '.join(supported)))
}

do_install() {

    # determine build type based on what flutter-engine installed
    FLUTTER_RUNTIME_MODES="$(ls ${STAGING_DIR_TARGET}${datadir}/flutter/${FLUTTER_SDK_VERSION})"

    for FLUTTER_RUNTIME_MODE in $FLUTTER_RUNTIME_MODES; do

        if ! echo "${FLUTTER_APP_RUNTIME_MODES}" | grep -qw "$FLUTTER_RUNTIME_MODE"; then
            bbnote "Skipping install for: ${FLUTTER_RUNTIME_MODE}"
            continue
        fi

        bbnote "[${FLUTTER_RUNTIME_MODE}] Flutter Bundle Assets: Installing"

        # App artifacts
        install -d ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/data/flutter_assets
        install -d ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib

        if [ ! -z "${APP_CONFIG}" ]; then
            install -D -m 0644 ${UNPACKDIR}/${APP_CONFIG} \
                ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/config.toml
        fi

        cp -r ${S}/${FLUTTER_APPLICATION_PATH}/build/flutter_assets/* \
            ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/data/flutter_assets/

        if [ "${FLUTTER_RUNTIME_MODE}" = "release" ] || [ "${FLUTTER_RUNTIME_MODE}" = "profile" ]; then

            bbnote "[${FLUTTER_RUNTIME_MODE}] Flutter AOT: Installing ${FLUTTER_RUNTIME_MODE}"

            install -d ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib
            cp ${S}/${FLUTTER_APPLICATION_PATH}/libapp.so \
                ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib/
        fi

        bbnote "[${FLUTTER_RUNTIME_MODE}] Flutter Bundle Symlink: Installing"

        # Engine artifact symlinks
        ln -sfr ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/data/icudtl.dat \
            ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/data/icudtl.dat

        ln -sfr ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib/libflutter_engine.so \
            ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib/libflutter_engine.so
    done
}

INSANE_SKIP:${PN} += " ldflags libdir dev-so buildpaths"

FILES:${PN} = "\
    ${bindir} \
    ${libdir} \
    ${FLUTTER_INSTALL_DIR} \
    "

RDEPENDS:${PN} = "flutter-engine"
