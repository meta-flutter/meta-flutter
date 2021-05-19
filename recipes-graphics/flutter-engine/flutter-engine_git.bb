SUMMARY = "Flutter Engine"
DESCRIPTION = "recipe to build Google Flutter Engine for use with Dart applications on embedded Linux"
AUTHOR = "joel.winarske@linux.com"
HOMEPAGE = "https://github.com/jwinarske/meta-flutter/"
BUGTRACKER = "https://github.com/jwinarske/meta-flutter/issues"
SECTION = "graphics"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://flutter/LICENSE;md5=a60894397335535eb10b54e2fff9f265"
CVE_PRODUCT = "libflutter_engine.so"

DEPENDS += "depot-tools-native \
            fontconfig \
            zip-native"

SRC_URI = "file://0001-clang-toolchain.patch \
           file://0002-x64-sysroot-assert.patch"

S = "${WORKDIR}/src"

FLUTTER_CHANNEL ??= "beta"
FLUTTER_CLANG_VERSION ??= "12.0.0"
TARGET_GCC_VERSION ??= "9.3.0"
DEPOT_TOOLS_PYTHON2_PATH ??= "depot_tools/bootstrap-2@3.8.9.chromium.14_bin/python/bin"

inherit python3native

require gn-utils.inc

SRCREV ??= "${@gn_get_channel_commit(d)}"

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE_aarch64 = "(.*)"
COMPATIBLE_MACHINE_armv7a = "(.*)"
COMPATIBLE_MACHINE_armv7ve = "(.*)"
COMPATIBLE_MACHINE_x86 = "(.*)"
COMPATIBLE_MACHINE_x86-64 = "(.*)"

PACKAGECONFIG ?= "disable-desktop-embeddings \
                  embedder-for-target \
                  fontconfig \
                  full-dart-sdk \
                  mode-release \
                 "

PACKAGECONFIG[asan] = "--asan"
PACKAGECONFIG[coverage] = "--coverage"
PACKAGECONFIG[dart-debug] = "--dart-debug"
PACKAGECONFIG[disable-desktop-embeddings] = "--disable-desktop-embeddings"
PACKAGECONFIG[embedder-for-target] = "--embedder-for-target"
PACKAGECONFIG[fontconfig] = "--enable-fontconfig"
PACKAGECONFIG[full-dart-debug] = "--full-dart-debug"
PACKAGECONFIG[full-dart-sdk] = "--full-dart-sdk"
PACKAGECONFIG[interpreter] = "--interpreter"
PACKAGECONFIG[lsan] = "--lsan"
PACKAGECONFIG[mode-debug] = "--runtime-mode debug"
PACKAGECONFIG[mode-jit_release] = "--runtime-mode jit_release"
PACKAGECONFIG[mode-profile] = "--runtime-mode profile"
PACKAGECONFIG[mode-release] = "--runtime-mode release"
PACKAGECONFIG[msan] = "--msan"
PACKAGECONFIG[skshaper] = "--enable-skshaper"
PACKAGECONFIG[static-analyzer] = "--clang-static-analyzer"
PACKAGECONFIG[tsan] = "--tsan"
PACKAGECONFIG[ubsan] = "--ubsan"
PACKAGECONFIG[unoptimized] = "--unoptimized"
PACKAGECONFIG[vulkan] = "--enable-vulkan"
PACKAGECONFIG[vulkan-validation-layers] = "--enable-vulkan-validation-layers"

CLANG_TOOLCHAIN_TRIPLE = "${@gn_clang_triple_prefix(d)}"
CLANG_PATH = "${WORKDIR}/src/buildtools/linux-x64/clang"
CLANG_INSTALL_DIR = "${CLANG_PATH}/lib/clang/${FLUTTER_CLANG_VERSION}/lib/${CLANG_TOOLCHAIN_TRIPLE}"
GCC_OBJ_DIR = "${STAGING_LIBDIR}/${TARGET_SYS}/${TARGET_GCC_VERSION}"

ARGS_GN_APPEND_aarch64 = "arm_tune = \"${@gn_get_tune_features(d)}\""
ARGS_GN_APPEND_armv7a  = "arm_tune = \"${TUNEABI}\" arm_float_abi = \"${TARGET_FPU}\""
ARGS_GN_APPEND_armv7ve = "arm_tune = \"${TUNEABI}\" arm_float_abi = \"${TARGET_FPU}\""

OUT_DIR_REL = "${@get_out_dir(d)}"

ARGS_GN_FILE = "${WORKDIR}/src/${OUT_DIR_REL}/args.gn"

GN_ARGS = "${PACKAGECONFIG_CONFARGS} --clang --lto --no-goma"
GN_ARGS_append = " --target-os linux"
GN_ARGS_append = " --linux-cpu ${@gn_target_arch_name(d)}"
GN_ARGS_append = " --target-sysroot ${STAGING_DIR_TARGET}"
GN_ARGS_append = " --target-toolchain ${CLANG_PATH}"
GN_ARGS_append = " --target-triple ${CLANG_TOOLCHAIN_TRIPLE}"

do_patch_prepend() {

    export PATH=${STAGING_BINDIR_NATIVE}/depot_tools:${STAGING_BINDIR_NATIVE}/${DEPOT_TOOLS_PYTHON2_PATH}:$PATH

    bbnote "ARGS: ${GN_ARGS}"
    bbnote "ARGS_GN_FILE: ${ARGS_GN_FILE}"
    bbnote "CLANG_TOOLCHAIN_TRIPLE: ${CLANG_TOOLCHAIN_TRIPLE}"
    bbnote "GCLIENT_ROOT: ${S}/.."
    bbnote "FLUTTER_CHANNEL: ${SRCREV}"
    bbnote "OUT_DIR_REL: ${OUT_DIR_REL}"
    bbnote "gclient sync --shallow --no-history -R -D --revision ${SRCREV} ${PARALLEL_MAKE} -v"
}

do_patch() {

    cd ${WORKDIR}

    echo 'solutions = [
        {
            "managed" : False,
            "name" : "src/flutter",
            "url" : "https://github.com/flutter/engine.git",
            "deps_file": "DEPS",
            "custom_vars" : {
                "download_android_deps" : False,
                "download_windows_deps" : False,
                "download_linux_deps"   : False,
            }
        }
    ]' > .gclient

    ##################################
    # depot_tools dependent variables
    ##################################

    # don't auto update...
    export DEPOT_TOOLS_UPDATE=0
    # force python 2
    export GCLIENT_PY3=0

    #################################################################
    # --shallow:    Do a shallow clone into the cache dir
    # --no-history: No git history to minimize download
    # -R:           resets any local changes before updating
    # -D:           Deletes from the working copy any dependencies that
    #               have been removed since the last sync
    # --revision:   Enforces revision/hash for the solutions with the format src@rev
    # -j:           Specify how many SCM commands can run in parallel
    #################################################################
    gclient sync --shallow --no-history -R -D --revision ${SRCREV} ${PARALLEL_MAKE} -v

    cd ${S}

    git apply ${WORKDIR}/0001-clang-toolchain.patch
    git apply ${WORKDIR}/0002-x64-sysroot-assert.patch

    # required object files to link
    install -d "${CLANG_INSTALL_DIR}"
    install -m 644 "${GCC_OBJ_DIR}/crtbeginS.o" "${CLANG_INSTALL_DIR}/"
    install -m 644 "${GCC_OBJ_DIR}/crtendS.o" "${CLANG_INSTALL_DIR}/"
}
do_patch[depends] += "depot-tools-native:do_populate_sysroot"
do_patch[depends] += "fontconfig:do_populate_sysroot"

do_configure_prepend() {

    export PATH=${STAGING_BINDIR_NATIVE}/depot_tools:${STAGING_BINDIR_NATIVE}/${DEPOT_TOOLS_PYTHON2_PATH}:$PATH
}

do_configure() {

    cd ${S}

    ./flutter/tools/gn ${GN_ARGS}

    echo "${ARGS_GN_APPEND}" >> ${ARGS_GN_FILE}
}
do_configure[depends] += "depot-tools-native:do_populate_sysroot"

do_compile_prepend() {

    export PATH=${STAGING_BINDIR_NATIVE}/depot_tools:${STAGING_BINDIR_NATIVE}/${DEPOT_TOOLS_PYTHON2_PATH}:$PATH
}

do_compile() {

    cd ${S}

    autoninja -C ${OUT_DIR_REL}
}
do_compile[depends] += "depot-tools-native:do_populate_sysroot"
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    install -d                                                            ${D}/${libdir}
    install -m 644 ${S}/${OUT_DIR_REL}/so.unstripped/libflutter_engine.so ${D}/${libdir}

    install -d                                                            ${D}/${includedir}
    install -m 644 ${S}/${OUT_DIR_REL}/flutter_embedder.h                 ${D}/${includedir}

    install -d                                                            ${D}/${datadir}/flutter
    install -m 644 ${S}/${OUT_DIR_REL}/icudtl.dat                         ${D}/${datadir}/flutter/

    # create SDK
    install -d                                                            ${D}/${datadir}/flutter/sdk
    echo "${SRCREV}" > ${D}/usr/share/flutter/sdk/engine.version
    install -m 644 ${S}/${OUT_DIR_REL}/flutter_patched_sdk/*              ${D}/${datadir}/flutter/sdk/
    install -m 644 ${S}/${OUT_DIR_REL}/gen/frontend_server.dart.snapshot  ${D}/${datadir}/flutter/sdk/

    install -d                                                            ${D}/${datadir}/flutter/sdk/clang_x64
    install -m 755 ${S}/${OUT_DIR_REL}/clang_x64/gen_snapshot             ${D}/${datadir}/flutter/sdk/clang_x64/

    cd ${D}/${datadir}/flutter
    zip -r engine_sdk.zip sdk
    rm -rf sdk
}
do_install[depends] += "zip-native:do_populate_sysroot"

FILES_${PN} = "${libdir} \
               ${datadir}/flutter \
              "

FILES_${PN}-dev = "${includedir}"

BBCLASSEXTEND = ""

# vim:set ts=4 sw=4 sts=4 expandtab:

