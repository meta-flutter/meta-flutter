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
require conf/include/gn-utils.inc

DEPENDS += " \
    flutter-engine \
    flutter-engine-sdk-native \
    "

BUILD_TYPE = "app"

# Force the Flutter build to target the cross-compile machine architecture
# rather than the build host (flutter defaults to `uname -m`). Use the
# target-arch mapping (aarch64 -> arm64, x86-64 -> x64), not clang_build_arch()
# which reports BUILD_ARCH. Use :append (not +=) so the ??= default survives.
FLUTTER_BUILD_ARGS ??= "bundle"
FLUTTER_BUILD_ARGS:append = " --target-platform linux-${@gn_target_arch_name(d)}"

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

# buildpaths is deliberately not skipped. The AOT image used to embed the
# absolute path of the plugin registrant, and suppressing the check here is
# what let that ship unnoticed; with the registrant named through a
# filesystem-root scheme (see common.inc) an app package has no build path
# left in it, so the check is a regression guard rather than noise.
INSANE_SKIP:${PN} += " ldflags libdir dev-so"

FILES:${PN} = "\
    ${bindir} \
    ${libdir} \
    ${FLUTTER_INSTALL_DIR} \
    "

RDEPENDS:${PN} = "flutter-engine"

# An app that ships without its plugins registered is indistinguishable from
# one that has none: no error, no missing file, just an app whose plugins do
# nothing. The generated registrant is the only library an app links that has
# no package: URI, so nothing downstream records whether it made it.
#
# Check the packaged image, which is what ships. Apps with no Linux plugins
# legitimately have no registrant, so the plugin list decides whether its
# absence is a fault -- the same test conf/include/common.inc applies before
# building the snapshot.
python flutter_check_packaged_registrant() {
    import json
    import os

    pkgdest = d.getVar('PKGDEST')
    if not pkgdest or not os.path.isdir(pkgdest):
        return

    app_dir = os.path.join(d.getVar('S'),
                           d.getVar('FLUTTER_APPLICATION_PATH') or '')
    plugins_json = os.path.join(app_dir, '.flutter-plugins-dependencies')
    linux_plugins = []
    if os.path.isfile(plugins_json):
        with open(plugins_json, 'r') as f:
            linux_plugins = [p['name'] for p in
                             json.load(f).get('plugins', {}).get('linux', [])]

    for dirpath, _, files in os.walk(pkgdest):
        if 'libapp.so' not in files:
            continue
        path = os.path.join(dirpath, 'libapp.so')
        if os.path.islink(path):
            continue
        with open(path, 'rb') as f:
            count = f.read().count(b'_PluginRegistrant')
        rel = os.path.relpath(path, pkgdest)
        if linux_plugins and not count:
            bb.fatal(
                f'{rel} carries no plugin registrant, but this app declares '
                f'Linux plugins ({", ".join(linux_plugins)}).\n'
                f'It would install and run with those plugins silently inert.')
        bb.note(f'plugin registrant in {rel}: {count}')
}
do_package[postfuncs] += "flutter_check_packaged_registrant"
