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
    compiler-rt \
    libcxx \
    zip-native \
    ${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'wayland', '', d)} \
    "

FLUTTER_ENGINE_PATCHES ?= "\
    file://0001-clang-toolchain.patch \
    file://0001-disable-pre-canned-sysroot.patch \
    file://0001-remove-x11-dependency.patch \
    file://0001-Disable-x11.patch \
    file://0001-remove-angle.patch \
    "

SRC_URI = "\
    gn://github.com/flutter/engine.git;name=flutter \
    ${FLUTTER_ENGINE_PATCHES} \
    "

S = "${WORKDIR}/src"

inherit gn-for-flutter python3native features_check pkgconfig

require conf/include/gn-utils.inc
require conf/include/flutter-version.inc


# For gn.bbclass
GN_CUSTOM_VARS ?= '\
{\
    "download_android_deps": False, \
    "download_windows_deps": False, \
    "download_linux_deps": False,   \
}'
EXTRA_GN_SYNC ?= "--shallow --no-history -R -D"

# For do_configure, do_compile
RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE:aarch64 = "(.*)"
COMPATIBLE_MACHINE:armv7 = "(.*)"
COMPATIBLE_MACHINE:armv7a = "(.*)"
COMPATIBLE_MACHINE:armv7ve = "(.*)"
COMPATIBLE_MACHINE:x86 = "(.*)"
COMPATIBLE_MACHINE:x86-64 = "(.*)"

PACKAGECONFIG ??= "\
    debug profile release \
    embedder-for-target \
    fontconfig \
    mallinfo2 \
    ${@bb.utils.contains('DISTRO_FEATURES', 'vulkan', 'vulkan impeller-vulkan', '', d)} \
    impeller-playground \
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
PACKAGECONFIG[impeller-playground] = "--enable-impeller-playground"
PACKAGECONFIG[jit_release] = "--runtime-mode jit_release"
PACKAGECONFIG[lsan] = "--lsan"
PACKAGECONFIG[mallinfo2] = "--use-mallinfo2"
PACKAGECONFIG[msan] = "--msan"
PACKAGECONFIG[prebuilt-dart-sdk] = "--prebuilt-dart-sdk,--no-prebuilt-dart-sdk"
PACKAGECONFIG[profile] = "--runtime-mode profile"
PACKAGECONFIG[release] = "--runtime-mode release"
PACKAGECONFIG[skshaper] = "--enable-skshaper"
PACKAGECONFIG[static-analyzer] = "--clang-static-analyzer"
PACKAGECONFIG[tsan] = "--tsan"
PACKAGECONFIG[trace-gn] = "--trace-gn"
PACKAGECONFIG[ubsan] = "--ubsan"
PACKAGECONFIG[unoptimized] = "--unoptimized"
PACKAGECONFIG[verbose] = "--verbose"
PACKAGECONFIG[vulkan] = "--enable-vulkan,, wayland"
PACKAGECONFIG[impeller-vulkan] = "--enable-impeller-vulkan"



CLANG_TOOLCHAIN_TRIPLE = "${@gn_clang_triple_prefix(d)}"
CLANG_PATH = "${WORKDIR}/src/buildtools/linux-x64/clang"

GN_ARGS = '\
    ${PACKAGECONFIG_CONFARGS} \
    --clang --lto --no-goma --no-stripped \
    --target-os linux \
    --linux-cpu ${@gn_target_arch_name(d)} \
    --target-sysroot ${STAGING_DIR_TARGET} \
    --target-toolchain ${CLANG_PATH} \
    --target-triple ${@gn_clang_triple_prefix(d)} \
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


do_compile() {

    FLUTTER_RUNTIME_MODES="${@bb.utils.filter('PACKAGECONFIG', 'debug profile release jit_release', d)}"
    bbnote "FLUTTER_RUNTIME_MODES=${FLUTTER_RUNTIME_MODES}"

    for MODE in $FLUTTER_RUNTIME_MODES; do

        # make it easy to parse
        BUILD_DIR="$(echo ${TMP_OUT_DIR} | sed "s/_RUNTIME_/${MODE}/g")"
        ARGS_FILE="${WORKDIR}/src/${BUILD_DIR}/args.gn"

        # remove in case this is a rebuild and you're not using rm_work.bbclass
        rm -rf ${WORKDIR}/src/${BUILD_DIR} | true

        ./flutter/tools/gn ${GN_ARGS_LESS_RUNTIME_MODES} --runtime-mode $MODE

        echo ${GN_TUNE_ARGS} >> "${ARGS_FILE}"

        bbnote `cat ${ARGS_FILE}`

        autoninja -C ${BUILD_DIR}
    done
}
do_compile[depends] += "depot-tools-native:do_populate_sysroot"
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    FLUTTER_RUNTIME_MODES="${@bb.utils.filter('PACKAGECONFIG', 'debug profile release jit_release', d)}"
    bbnote "FLUTTER_RUNTIME_MODES=${FLUTTER_RUNTIME_MODES}"

    for MODE in $FLUTTER_RUNTIME_MODES; do

        BUILD_DIR="$(echo ${TMP_OUT_DIR} | sed "s/_RUNTIME_/${MODE}/g")"

        install -D -m 0644 ${S}/${BUILD_DIR}/so.unstripped/libflutter_engine.so \
            ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${MODE}/lib/libflutter_engine.so

        if ${@bb.utils.contains('PACKAGECONFIG', 'desktop-embeddings', 'true', 'false', d)}; then
            install -D -m 0644 ${S}/${BUILD_DIR}/so.unstripped/libflutter_linux_gtk.so \
                ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${MODE}/lib/libflutter_linux_gtk.so
        fi

        install -D -m 0644 ${S}/${BUILD_DIR}/flutter_embedder.h ${D}${includedir}/flutter_embedder.h

        install -D -m 0644 ${S}/${BUILD_DIR}/icudtl.dat \
            ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${MODE}/data/icudtl.dat

        # create SDK
        install -D -m 0755 ${S}/${BUILD_DIR}/clang_x64/exe.unstripped/analyze_snapshot \
            ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${MODE}/sdk/clang_x64/analyze_snapshot || true
        install -D -m 0755 ${S}/${BUILD_DIR}/clang_x64/exe.unstripped/dart \
            ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${MODE}/sdk/clang_x64/dart || true
        install -D -m 0755 ${S}/${BUILD_DIR}/clang_x64/exe.unstripped/flatc \
            ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${MODE}/sdk/clang_x64/flatc || true
        
        if ${@bb.utils.contains('PACKAGECONFIG', 'impeller-vulkan', 'true', 'false', d)}; then
            install -D -m 0755 ${S}/${BUILD_DIR}/clang_x64/exe.unstripped/blobcat \
                ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${MODE}/sdk/clang_x64/blobcat
            install -D -m 0755 ${S}/${BUILD_DIR}/clang_x64/exe.unstripped/impellerc \
                ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${MODE}/sdk/clang_x64/impellerc
        fi

        install -D -m 0755 ${S}/${BUILD_DIR}/clang_x64/exe.unstripped/gen_snapshot \
            ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${MODE}/sdk/clang_x64/gen_snapshot
            
        cd ${S}/flutter
        echo $SRCREV                   > ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${MODE}/sdk/engine.version
        echo $FLUTTER_ENGINE_REPO_URL >> ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${MODE}/sdk/engine.version
        echo $FLUTTER_SDK_VERSION     >> ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${MODE}/sdk/flutter_sdk.version
        echo $RUNTIME_MODE            >> ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${MODE}/sdk/flutter.runtime

        cd ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${MODE}/
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
