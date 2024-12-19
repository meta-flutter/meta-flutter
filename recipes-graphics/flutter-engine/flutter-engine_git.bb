#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
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

DEPENDS:aarch64 += "\
    freetype \
    "

PV = "${FLUTTER_SDK_VERSION}"

S = "${WORKDIR}/src"

SRC_URI_EXTRA = ""

SRC_URI = "\
    gn://github.com/flutter/engine.git;gn_name=src/flutter \
    file://BUILD.gn.in \
    ${SRC_URI_EXTRA} \
    "

# musl-specific patches.
SRC_URI:append:libc-musl = "\
    file://0002-libcxx-uglify-support-musl.patch;patchdir=flutter/third_party \
    file://0003-libcxx-return-type-in-wcstoull_l.patch;patchdir=flutter/third_party \
    file://0004-suppres-musl-libc-warning.patch;patchdir=flutter/third_party/dart \
    "

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

PACKAGECONFIG ??= "\
    desktop-embeddings \
    debug profile release \
    embedder-for-target \
    fontconfig \
    mallinfo2 \
    impeller-3d \
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
PACKAGECONFIG[vulkan] = "--enable-vulkan"
PACKAGECONFIG[impeller-3d] = "--enable-impeller-3d"

CLANG_BUILD_ARCH = "${@clang_build_arch(d)}"
CLANG_TOOLCHAIN_TRIPLE = "${@gn_clang_triple_prefix(d)}"
CLANG_PATH = "${WORKDIR}/src/flutter/buildtools/linux-${CLANG_BUILD_ARCH}/clang"

GN_ARGS = "\
    ${PACKAGECONFIG_CONFARGS} \
    --clang --lto \
    --no-goma --no-rbe \
    --no-enable-unittests \
    --no-stripped \
    --target-os linux \
    --linux-cpu ${@gn_target_arch_name(d)} \
    --target-sysroot ${STAGING_DIR_TARGET} \
    --target-toolchain ${CLANG_PATH} \
    --target-triple ${@gn_clang_triple_prefix(d)} \
    "

GN_ARGS:append:libc-musl = "\
    --no-backtrace \
    "

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
FLUTTER_ENGINE_CXX_LIBC_FLAGS ?= ""
FLUTTER_ENGINE_CXX_LIBC_FLAGS:append:libc-musl = "-D_LIBCPP_HAS_MUSL_LIBC"

WAYLAND_IS_PRESENT="${@bb.utils.filter('DISTRO_FEATURES', 'wayland', d)}"
X11_IS_PRESENT="${@bb.utils.filter('DISTRO_FEATURES', 'x11', d)}"
VULKAN_HEADER_GNI = "${S}/flutter/build_overrides/vulkan_headers.gni"


do_configure() {

    #
    # disable default sysroot
    #

    sed -i "s|use_default_linux_sysroot = true|use_default_linux_sysroot = false|g" ${S}/build/config/sysroot.gni

    #
    # vulkan_headers override: enables DRM case
    #

    test -z $WAYLAND_IS_PRESENT && sed -i "s|vulkan_use_wayland = true|vulkan_use_wayland = false|g" ${VULKAN_HEADER_GNI}
    test -z $X11_IS_PRESENT     && sed -i "s|vulkan_use_x11 = true|vulkan_use_x11 = false|g" ${VULKAN_HEADER_GNI}

    #
    # remove x11 package check if x11 is not available
    #
    test -z $X11_IS_PRESENT     && sed -i '/^pkg_config("x11") {/,/^}$/d' ${S}/flutter/shell/platform/linux/config/BUILD.gn

    #
    # fix build without wayland
    #
    test -z $WAYLAND_IS_PRESENT && sed -i "s|ozone_platform_wayland = true|ozone_platform_wayland = false|g" ${S}/build/config/BUILDCONFIG.gn
    test -z $X11_IS_PRESENT && sed -i "s|ozone_platform_x11 = true|ozone_platform_x11 = false|g" ${S}/build/config/BUILDCONFIG.gn 

    #
    # fix build with musl libc
    #
    [ "${TCLIBC}" = "musl" ] && sed -i "s|#define HAVE_MALLINFO 1||g" -i ${S}/flutter/third_party/swiftshader/third_party/llvm-10.0/configs/linux/include/llvm/Config/config.h
    
    #
    # Custom Build config
    #
    cp ${WORKDIR}/BUILD.gn.in ${S}/build/toolchain/custom/BUILD.gn
    sed -i "s|@DEBUG_FLAGS@|${FLUTTER_ENGINE_DEBUG_FLAGS}|g" ${S}/build/toolchain/custom/BUILD.gn
    sed -i "s|@CXX_LIBC_FLAGS@|${FLUTTER_ENGINE_CXX_LIBC_FLAGS}|g" ${S}/build/toolchain/custom/BUILD.gn
    
    #
    # Configure each mode defined in PACKAGECONFIG
    #

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
    # required for dart: https://github.com/dart-lang/sdk/issues/41560
    export HOME=${WORKDIR}

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

        #
        # Install directories
        #
        install -d ${D}${includedir}
        install -d ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/lib
        install -d ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/data

        install -d ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/lib
        install -d ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/clang_${CLANG_BUILD_ARCH}
        install -d ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/flutter_patched_sdk

        #
        # Headers
        #
        install -m 0644 ${S}/${BUILD_DIR}/flutter_embedder.h ${D}${includedir}/flutter_embedder.h

        #
        # Shared modules
        #
        cwd=$(pwd)
        cd ${S}/${BUILD_DIR}/so.unstripped
        for file in *; do
            cp "$file" ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/lib/
            cp "../$file.TOC" ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/lib/
        done
        cd $cwd

        #
        # Data
        #
        install -m 0644 ${S}/${BUILD_DIR}/icudtl.dat ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/data/icudtl.dat

        test -e ${S}/${BUILD_DIR}/shader_lib && \
            cp -r ${S}/${BUILD_DIR}/shader_lib ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/lib/

        #
        # Executables
        #
        cwd=$(pwd)
        cd ${S}/${BUILD_DIR}/clang_${CLANG_BUILD_ARCH}/exe.unstripped
        for file in *; do
            # copy the unstripped variant one up
            cp "../$file" ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/clang_${CLANG_BUILD_ARCH}/
        done
        cd $cwd

        # include patched sdk for local-engine scenarios
        test -e ${S}/${BUILD_DIR}/flutter_patched_sdk && \
            cp -r ${S}/${BUILD_DIR}/flutter_patched_sdk ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/

        cd ${S}/flutter
        echo "${SRCREV}"                   > ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/engine.version
        echo "${FLUTTER_ENGINE_REPO_URL}" >> ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/engine.version
        echo "${FLUTTER_SDK_VERSION}"      > ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/flutter_sdk.version
        echo "${MODE}"                     > ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/flutter.runtime

        cp "${S}/${BUILD_DIR}/args.gn"       ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/sdk/args.gn

        cd ${D}${FLUTTER_ENGINE_INSTALL_PREFIX}/${MODE}/
        zip -r engine_sdk.zip sdk
        rm -rf sdk

    done
}
do_install[depends] += "zip-native:do_populate_sysroot"

PACKAGES =+ "${PN}-sdk-dev"

INSANE_SKIP:${PN} += " libdir"
INSANE_SKIP:${PN}-dbg += "libdir"

FILES:${PN} = "\
    ${datadir}/flutter \
    "

FILES:${PN}-dbg = "\
    ${FLUTTER_ENGINE_INSTALL_PREFIX}/*/lib/.debug \
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
