#
# Copyright (c) 2020-2024 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# Helper class for building Flutter Applications.
# Assumes that:
# - S is defined and points to source directory.
# - PUBSPEC_APPNAME is defined correctly.  This is the name value from pubspec.yml.
#

require conf/include/flutter-version.inc
require conf/include/app-utils.inc
require conf/include/common.inc
# gn_target_arch_name() and GN_TARGET_ARCH_NAME. Needed for the
# --target-platform append below, and by the supported-arch guard further
# down, which silently no-ops while GN_TARGET_ARCH_NAME is unset.
require conf/include/gn-utils.inc

DEPENDS += " \
    flutter-engine \
    flutter-engine-sdk-native \
    "

BUILD_TYPE = "app"

FLUTTER_BUILD_ARGS ??= "bundle"
# Without an explicit target platform the flutter tool defaults to Android
# (see meta-flutter#760), so a Linux bundle builds an AndroidAssetTarget and
# any package carrying a native-assets build hook fails the whole bundle on
# an SDK it will never use:
#
#   Target build_hooks failed: Error: Android SDK could not be found.
#
# Apps with no build hook never reach that code path, which is why this went
# unnoticed. Underscore override syntax: bitbake 1.46 has no colon form.
FLUTTER_BUILD_ARGS_append = " --target-platform linux-${@gn_target_arch_name(d)}"

# flutter build bundle only accepts the target platforms TargetPlatform
# carries. Upstream that is linux_x64, linux_arm64 and linux_riscv64; the
# flutter-sdk recipe patches in linux_arm, so 32-bit ARM builds too. x86 and
# riscv32 stay out: Dart has no ia32 AOT and no riscv32 at all, so gen_snapshot
# could not produce a libapp.so for them. Skip those at parse time rather than
# fail in do_compile after a full fetch and pub get.
FLUTTER_APP_SUPPORTED_ARCHS ?= "x64 arm64 riscv64 arm"

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
            install -D -m 0644 ${WORKDIR}/${APP_CONFIG} \
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

INSANE_SKIP_${PN} += " ldflags libdir dev-so"

FILES_${PN} = "\
    ${bindir} \
    ${libdir} \
    ${FLUTTER_INSTALL_DIR} \
    "

RDEPENDS_${PN} = "flutter-engine"
