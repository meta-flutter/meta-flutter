
REQUIRED_DISTRO_FEATURES = "opengl"

DEPENDS += "\
    ca-certificates-native \
    curl-native \
    depot-tools-native \
    fontconfig \
    zip-native \
    "

S = "${WORKDIR}/src"

inherit python3native features_check

require conf/include/gn-utils.inc

SRCREV ??= "${@gn_get_channel_commit(d)}"

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE_aarch64 = "(.*)"
COMPATIBLE_MACHINE_armv7 = "(.*)"
COMPATIBLE_MACHINE_armv7a = "(.*)"
COMPATIBLE_MACHINE_armv7ve = "(.*)"
COMPATIBLE_MACHINE_x86 = "(.*)"
COMPATIBLE_MACHINE_x86-64 = "(.*)"

PACKAGECONFIG ??= "disable-desktop-embeddings \
                  embedder-for-target \
                  fontconfig \
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

ARGS_GN_FILE = "${WORKDIR}/src/${OUT_DIR_REL}/args.gn"

OUT_DIR_REL = "${@get_out_dir(d)}"

GN_ARGS = "${PACKAGECONFIG_CONFARGS} --clang --lto --no-goma"
GN_ARGS_append = " --target-os linux"
GN_ARGS_append = " --linux-cpu ${@gn_target_arch_name(d)}"
GN_ARGS_append = " --target-sysroot ${STAGING_DIR_TARGET}"
GN_ARGS_append = " --target-toolchain ${CLANG_PATH}"
GN_ARGS_append = " --target-triple ${CLANG_TOOLCHAIN_TRIPLE}"

GN_ARGS_append_armv7 = " --arm-float-abi ${TARGET_FPU}"
GN_ARGS_append_armv7a = " --arm-float-abi ${TARGET_FPU}"
GN_ARGS_append_armv7ve = " --arm-float-abi ${TARGET_FPU}"

ARGS_GN = ""
ARGS_GN_append_aarch64 = "arm_tune = \"${@gn_get_tune_features(d)}\""
ARGS_GN_append_armv7 = "arm_tune = \"${@gn_get_tune_features(d)}\""
ARGS_GN_append_armv7a = "arm_tune = \"${@gn_get_tune_features(d)}\""
ARGS_GN_append_armv7ve = "arm_tune = \"${@gn_get_tune_features(d)}\""


do_patch_prepend() {

    export CURL_CA_BUNDLE=${STAGING_DIR_NATIVE}/etc/ssl/certs/ca-certificates.crt
    export PATH=${DEPOT_TOOLS}:${DEPOT_TOOLS}/${PYTHON2_PATH}:$PATH

    bbnote "ARGS: ${GN_ARGS}"
    bbnote "ARGS_GN_FILE: ${ARGS_GN_FILE}"
    bbnote "ARGS_GN: ${ARGS_GN}"
    bbnote "CLANG_TOOLCHAIN_TRIPLE: ${CLANG_TOOLCHAIN_TRIPLE}"
    bbnote "GCLIENT_ROOT: ${S}/.."
    bbnote "FLUTTER_CHANNEL: ${SRCREV}"
    bbnote "OUT_DIR_REL: ${OUT_DIR_REL}"
    bbnote "gclient sync --shallow --no-history -R -D --revision ${SRCREV} ${PARALLEL_MAKE} -v"
}

do_patch () {

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

    bbnote `python -V`
    bbnote `which python`
    bbnote `which gclient`

    #################################################################
    # --shallow:    Do a shallow clone into the cache dir
    # --no-history: No git history to minimize download
    # -R:           resets any local changes before updating
    # -D:           Deletes from the working copy any dependencies that
    #               have been removed since the last sync
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
do_patch[depends] += "ca-certificates-native:do_populate_sysroot"
do_patch[depends] += "depot-tools-native:do_populate_sysroot"
do_patch[depends] += "fontconfig:do_populate_sysroot"

do_configure() {

    export PATH=${DEPOT_TOOLS}:${DEPOT_TOOLS}/${PYTHON2_PATH}:$PATH

    cd ${S}

    ./flutter/tools/gn ${GN_ARGS}

    echo ${ARGS_GN} >> ${ARGS_GN_FILE}
}
do_configure[depends] += "depot-tools-native:do_populate_sysroot"

do_compile() {

    export PATH=${DEPOT_TOOLS}:${DEPOT_TOOLS}/${PYTHON2_PATH}:$PATH

    cd ${S}

    autoninja -C ${OUT_DIR_REL}
}
do_compile[depends] += "depot-tools-native:do_populate_sysroot"
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    install -d ${D}${libdir}
    install -d ${D}${includedir}
    install -d ${D}${datadir}/flutter
    install -d ${D}${datadir}/flutter/sdk
    install -d ${D}${datadir}/flutter/sdk/clang_x64

    install -m 644 ${S}/${OUT_DIR_REL}/so.unstripped/libflutter_engine.so ${D}${libdir}

    install -m 644 ${S}/${OUT_DIR_REL}/flutter_embedder.h ${D}${includedir}

    install -m 644 ${S}/${OUT_DIR_REL}/icudtl.dat ${D}${datadir}/flutter/

    # create SDK
    echo "${SRCREV}" > ${D}/usr/share/flutter/sdk/engine.version
    install -m 644 ${S}/${OUT_DIR_REL}/flutter_patched_sdk/* ${D}${datadir}/flutter/sdk/

    install -m 755 ${S}/${OUT_DIR_REL}/clang_x64/gen_snapshot ${D}${datadir}/flutter/sdk/clang_x64/

    cd ${D}/${datadir}/flutter
    zip -r engine_sdk.zip sdk
    rm -rf sdk
}
do_install[depends] += "zip-native:do_populate_sysroot"

FILES_${PN} = "\
    ${libdir} \
    ${datadir}/flutter \
    "

FILES_${PN}-dev = "\
    ${includedir} \
    "

BBCLASSEXTEND = ""
