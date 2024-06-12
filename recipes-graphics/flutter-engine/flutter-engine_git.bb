#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Flutter Engine"
DESCRIPTION = "Google Flutter Engine for use with Flutter applications"
AUTHOR = "Flutter Team"
HOMEPAGE = "https://github.com/flutter/engine/"
BUGTRACKER = "https://github.com/flutter/flutter/issues"
SECTION = "graphics"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://flutter/LICENSE;md5=a60894397335535eb10b54e2fff9f265"
CVE_PRODUCT = "libflutter_engine.so"

REQUIRED_DISTRO_FEATURES = "opengl"

DEPENDS += "\
    zip-native \
    ${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'wayland', '', d)} \
    "

VULKAN_BACKENDS="${@bb.utils.filter('DISTRO_FEATURES', 'wayland x11', d)}"

PV = "${FLUTTER_SDK_VERSION}"

FLUTTER_ENGINE_PATCHES ?= "\
    file://BUILD.gn.in \
    file://0001-disable-pre-canned-sysroot.patch \
    file://0001-remove-x11-dependency.patch \
    file://0001-disable-x11.patch \
    file://0001-impeller-workaround.patch \
    file://0001-Skip-configuration-dependency-if-unit-tests-are-disa.patch \
    file://0001-gn-riscv32-and-riscv64.patch \
    file://0001-fml-build-config-add-riscv.patch \
    "

SRC_URI = "\
    gn://github.com/flutter/engine.git;gn_name=src/flutter \
    ${FLUTTER_ENGINE_PATCHES} \
    "

S = "${WORKDIR}/src"

inherit gn-fetcher features_check pkgconfig

require conf/include/gn-utils.inc
require conf/include/clang-utils.inc
require conf/include/flutter-version.inc


# For gn.bbclass
GN_CUSTOM_VARS ?= '\
{\
    "download_android_deps": False, \
    "download_windows_deps": False, \
    "download_linux_deps": False,   \
}'
EXTRA_GN_SYNC ?= "--shallow --no-history -R -D"

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE:aarch64 = "(.*)"
COMPATIBLE_MACHINE:armv7 = "(.*)"
COMPATIBLE_MACHINE:armv7a = "(.*)"
COMPATIBLE_MACHINE:armv7ve = "(.*)"
COMPATIBLE_MACHINE:x86 = "(.*)"
COMPATIBLE_MACHINE:x86-64 = "(.*)"
COMPATBILE_MACHINE:riscv32 = "(.*)"
COMPATIBLE_MACHINE:riscv64 = "(.*)"

PACKAGECONFIG ??= "\
    debug profile release \
    embedder-for-target \
    fontconfig \
    mallinfo2 \
    impeller-3d \
    ${@bb.utils.contains('DISTRO_FEATURES', 'opengl', 'impeller-opengles', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'vulkan', 'vulkan impeller-vulkan', '', d)} \
    "

PACKAGECONFIG[asan] = "--asan"
PACKAGECONFIG[coverage] = "--coverage"
PACKAGECONFIG[dart-debug] = "--dart-debug"
PACKAGECONFIG[debug] = "--runtime-mode debug"
PACKAGECONFIG[desktop-embeddings] = ",--disable-desktop-embeddings, glib-2.0 gtk+3"
PACKAGECONFIG[embedder-examples] = "--build-embedder-examples,--no-build-embedder-examples"
PACKAGECONFIG[embedder-for-target] = "--embedder-for-target"
PACKAGECONFIG[fontconfig] = "--enable-fontconfig,,fontconfig"
PACKAGECONFIG[full-dart-debug] = "--full-dart-debug"
PACKAGECONFIG[full-dart-sdk] = "--full-dart-sdk"
PACKAGECONFIG[glfw-shell] = "--build-glfw-shell,--no-build-glfw-shell, glfw"
PACKAGECONFIG[interpreter] = "--interpreter"
PACKAGECONFIG[jit_release] = "--runtime-mode jit_release"
PACKAGECONFIG[lsan] = "--lsan"
PACKAGECONFIG[mallinfo2] = "--use-mallinfo2"
PACKAGECONFIG[msan] = "--msan"
PACKAGECONFIG[prebuilt-dart-sdk] = "--prebuilt-dart-sdk,--no-prebuilt-dart-sdk"
PACKAGECONFIG[profile] = "--runtime-mode profile"
PACKAGECONFIG[release] = "--runtime-mode release"
PACKAGECONFIG[static-analyzer] = "--clang-static-analyzer"
PACKAGECONFIG[tsan] = "--tsan"
PACKAGECONFIG[trace-gn] = "--trace-gn"
PACKAGECONFIG[ubsan] = "--ubsan"
PACKAGECONFIG[unoptimized] = "--unoptimized"
PACKAGECONFIG[verbose] = "--verbose"
PACKAGECONFIG[vulkan] = "--enable-vulkan,, wayland"
PACKAGECONFIG[impeller-opengles] = "--enable-impeller-opengles"
PACKAGECONFIG[impeller-vulkan] = "--enable-impeller-vulkan"
PACKAGECONFIG[impeller-3d] = "--enable-impeller-3d"

CLANG_BUILD_ARCH = "${@clang_build_arch(d)}"
CLANG_TOOLCHAIN_TRIPLE = "${@gn_clang_triple_prefix(d)}"
CLANG_PATH = "${WORKDIR}/src/buildtools/linux-${CLANG_BUILD_ARCH}/clang"

GN_ARGS = '\
    ${PACKAGECONFIG_CONFARGS} \
    --clang --lto --no-goma --no-stripped \
    --target-os linux \
    --linux-cpu ${@gn_target_arch_name(d)} \
    --target-sysroot ${STAGING_DIR_TARGET} \
    --target-toolchain ${CLANG_PATH} \
    --target-triple ${@gn_clang_triple_prefix(d)} \
    --no-enable-unittests \
'

GN_ARGS:append:armv7 = " --arm-float-abi ${TARGET_FPU}"
GN_ARGS:append:armv7a = " --arm-float-abi ${TARGET_FPU}"
GN_ARGS:append:armv7ve = " --arm-float-abi ${TARGET_FPU}"

GN_TUNE_ARGS = ""
GN_TUNE_ARGS:append:aarch64 = "arm_tune = \"${@gn_get_tune_features(d)}\""
GN_TUNE_ARGS:append:armv7 = "arm_tune = \"${@gn_get_tune_features(d)}\""
GN_TUNE_ARGS:append:armv7a = "arm_tune = \"${@gn_get_tune_features(d)}\""
GN_TUNE_ARGS:append:armv7ve = "arm_tune = \"${@gn_get_tune_features(d)}\""

TMP_OUT_DIR="${@get_gn_tmp_out_dir(d)}"

GN_ARGS_LESS_RUNTIME_MODES="${@get_gn_args_less_runtime(d)}"

FLUTTER_ENGINE_INSTALL_PREFIX ??= "${datadir}/flutter/${FLUTTER_SDK_VERSION}"

FLUTTER_ENGINE_DEBUG_PREFIX_MAP ?= " \
 -fmacro-prefix-map=${S}=${TARGET_DBGSRC_DIR} \
 -fdebug-prefix-map=${S}=${TARGET_DBGSRC_DIR} \
 -fmacro-prefix-map=${B}=${TARGET_DBGSRC_DIR} \
 -fdebug-prefix-map=${B}=${TARGET_DBGSRC_DIR} \
 -fdebug-prefix-map=${STAGING_DIR_HOST}= \
 -fmacro-prefix-map=${STAGING_DIR_HOST}= \
 -fdebug-prefix-map=${STAGING_DIR_NATIVE}= \
"
FLUTTER_ENGINE_DEBUG_FLAGS ?= "-g -feliminate-unused-debug-types ${FLUTTER_ENGINE_DEBUG_PREFIX_MAP}"

FLUTTER_ENGINE_STRIP ??= "release"

do_configure() {
    
    # prevent tmp path warning
    cp ${WORKDIR}/BUILD.gn.in ${S}/build/toolchain/custom/BUILD.gn
    sed -i "s|@DEBUG_FLAGS@|${FLUTTER_ENGINE_DEBUG_FLAGS}|g" ${S}/build/toolchain/custom/BUILD.gn

    FLUTTER_RUNTIME_MODES="${@bb.utils.filter('PACKAGECONFIG', 'debug profile release jit_release', d)}"
    bbnote "FLUTTER_RUNTIME_MODES=${FLUTTER_RUNTIME_MODES}"
    bbnote "CLANG_BUILD_ARCH=${CLANG_BUILD_ARCH}"

    for MODE in $FLUTTER_RUNTIME_MODES; do

        # make it easy to parse
        BUILD_DIR="$(echo ${TMP_OUT_DIR} | sed "s/_RUNTIME_/${MODE}/g")"
        ARGS_FILE="${WORKDIR}/src/${BUILD_DIR}/args.gn"

        # remove in case this is a rebuild and you're not using rm_work.bbclass
        rm -rf ${WORKDIR}/src/${BUILD_DIR} | true

        ./flutter/tools/gn ${GN_ARGS_LESS_RUNTIME_MODES} --runtime-mode ${MODE}

        echo ${GN_TUNE_ARGS} >> "${ARGS_FILE}"

        bbnote `cat ${ARGS_FILE}`
    done
}
do_configure[depends] += "depot-tools-native:do_populate_sysroot"

do_compile() {

    FLUTTER_RUNTIME_MODES="${@bb.utils.filter('PACKAGECONFIG', 'debug profile release jit_release', d)}"
    bbnote "FLUTTER_RUNTIME_MODES=${FLUTTER_RUNTIME_MODES}"

    for MODE in $FLUTTER_RUNTIME_MODES; do
        BUILD_DIR="$(echo ${TMP_OUT_DIR} | sed "s/_RUNTIME_/${MODE}/g")"    
        ninja -C "${BUILD_DIR}" $PARALLEL_MAKE
    done
}
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    FLUTTER_RUNTIME_MODES="${@bb.utils.filter('PACKAGECONFIG', 'debug profile release jit_release', d)}"
    bbnote "FLUTTER_RUNTIME_MODES=${FLUTTER_RUNTIME_MODES}"

    for MODE in $FLUTTER_RUNTIME_MODES; do

        BUILD_DIR="$(echo ${TMP_OUT_DIR} | sed "s/_RUNTIME_/${MODE}/g")"

        install -D -m 0644 ${S}/${BUILD_DIR}/so.unstripped/libflutter_engine.so \
            ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/lib/libflutter_engine.so

        case "$FLUTTER_ENGINE_STRIP" in
        *$MODE*)
            ${STRIP} ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/$MODE/lib/libflutter_engine.so
            ;;
        esac

        if ${@bb.utils.contains('PACKAGECONFIG', 'desktop-embeddings', 'true', 'false', d)}; then
            install -D -m 0644 ${S}/${BUILD_DIR}/so.unstripped/libflutter_linux_gtk.so \
                ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/lib/libflutter_linux_gtk.so
        fi

        install -D -m 0644 ${S}/${BUILD_DIR}/flutter_embedder.h ${D}${includedir}/flutter_embedder.h

        install -D -m 0644 ${S}/${BUILD_DIR}/icudtl.dat \
            ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/data/icudtl.dat

        # create SDK - copy everything in exe.unstripped
        install -d ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/clang_${CLANG_BUILD_ARCH}
        cp ${S}/${BUILD_DIR}/clang_${CLANG_BUILD_ARCH}/exe.unstripped/* \
            ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/clang_${CLANG_BUILD_ARCH}/

        # strip executables
        for file in ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/clang_${CLANG_BUILD_ARCH}/*
        do
            strip "$file"
        done

        # include patched sdk for local-engine scenarios
        install -d ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/flutter_patched_sdk
        install -m 0644 ${S}/${BUILD_DIR}/flutter_patched_sdk/*.dill* \
            ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/flutter_patched_sdk || true
            
        cd ${S}/flutter
        echo "${SRCREV}"                   > ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/engine.version
        echo "${FLUTTER_ENGINE_REPO_URL}" >> ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/engine.version
        echo "${FLUTTER_SDK_VERSION}"      > ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/flutter_sdk.version
        echo "${MODE}"                     > ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/flutter.runtime

        cd ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/
        zip -r engine_sdk.zip sdk
        rm -rf sdk

    done
}
do_install[network] = "1"
do_install[depends] += "zip-native:do_populate_sysroot"

PACKAGES =+ "${PN}-sdk-dev"

INSANE_SKIP:${PN} += " libdir"

FILES:${PN} = "\
    ${datadir}/flutter \
    "

FILES:${PN}-sdk-dev = "\
    ${datadir}/flutter/${FLUTTER_SDK_TAG}/*/engine_sdk.zip \
    "

FILES:${PN}-dev = "\
    ${includedir} \
    "

python () {
    d.setVar('SRCREV', gn_get_engine_commit(d))

    d.setVar('FLUTTER_SDK_VERSION', get_flutter_sdk_version(d))
}
