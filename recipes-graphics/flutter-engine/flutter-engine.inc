SUMMARY = "Flutter Engine"
DESCRIPTION = "Google Flutter Engine for use with Flutter applications"
AUTHOR = "Flutter Team"
HOMEPAGE = "https://github.com/flutter/engineflutter/"
BUGTRACKER = "https://github.com/flutter/flutter/issues"
SECTION = "graphics"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://flutter/LICENSE;md5=a60894397335535eb10b54e2fff9f265"
CVE_PRODUCT = "libflutter_engine.so"

REQUIRED_DISTRO_FEATURES = "opengl"

DEPENDS += "\
    compiler-rt \
    fontconfig \
    libcxx \
    zip-native \
    "

SRC_URI += "file://0001-clang-toolchain.patch \
           file://0002-x64-sysroot-assert.patch"

S = "${WORKDIR}/git"

inherit python3native features_check pkgconfig

require conf/include/gn-utils.inc

FLUTTER_SDK_TAG ?= "${AUTOREV}"

# For do_configure, do_compile
RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE_aarch64 = "(.*)"
COMPATIBLE_MACHINE_armv7 = "(.*)"
COMPATIBLE_MACHINE_armv7a = "(.*)"
COMPATIBLE_MACHINE_armv7ve = "(.*)"
COMPATIBLE_MACHINE_x86 = "(.*)"
COMPATIBLE_MACHINE_x86-64 = "(.*)"

FLUTTER_RUNTIME ??= "release"

PACKAGECONFIG ??= "disable-desktop-embeddings \
                   disable-embedder-examples \
                   embedder-for-target \
                   fontconfig \
                   ${FLUTTER_RUNTIME} \
                  "

PACKAGECONFIG[asan] = "--asan"
PACKAGECONFIG[coverage] = "--coverage"
PACKAGECONFIG[dart-debug] = "--dart-debug"
PACKAGECONFIG[debug] = "--runtime-mode debug"
PACKAGECONFIG[disable-desktop-embeddings] = "--disable-desktop-embeddings"
PACKAGECONFIG[disable-embedder-examples] = "--no-build-embedder-examples"
PACKAGECONFIG[disable-glfw-shell] = "--no-build-glfw-shell"
PACKAGECONFIG[disable-prebuilt-dart-sdk] = "--no-prebuilt-dart-sdk"
PACKAGECONFIG[embedder-examples] = "--build-embedder-examples"
PACKAGECONFIG[embedder-for-target] = "--embedder-for-target"
PACKAGECONFIG[fontconfig] = "--enable-fontconfig"
PACKAGECONFIG[full-dart-debug] = "--full-dart-debug"
PACKAGECONFIG[full-dart-sdk] = "--full-dart-sdk"
PACKAGECONFIG[glfw-shell] = "--build-glfw-shell"
PACKAGECONFIG[interpreter] = "--interpreter"
PACKAGECONFIG[jit_release] = "--runtime-mode jit_release"
PACKAGECONFIG[lsan] = "--lsan"
PACKAGECONFIG[msan] = "--msan"
PACKAGECONFIG[profile] = "--runtime-mode profile"
PACKAGECONFIG[release] = "--runtime-mode release"
PACKAGECONFIG[skshaper] = "--enable-skshaper"
PACKAGECONFIG[static-analyzer] = "--clang-static-analyzer"
PACKAGECONFIG[tsan] = "--tsan"
PACKAGECONFIG[ubsan] = "--ubsan"
PACKAGECONFIG[unoptimized] = "--unoptimized"
PACKAGECONFIG[vulkan] = "--enable-vulkan"
PACKAGECONFIG[vulkan-validation-layers] = "--enable-vulkan-validation-layers"


CLANG_TOOLCHAIN_TRIPLE = "${@gn_clang_triple_prefix(d)}"
CLANG_PATH = "${S}/buildtools/linux-x64/clang"

ARGS_GN_FILE = "${S}/${OUT_DIR_REL}/args.gn"

OUT_DIR_REL = "${@get_out_dir(d)}"

GN_ARGS = "${PACKAGECONFIG_CONFARGS} --clang --lto --no-goma --no-stripped "
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

do_configure() {
    export http_proxy=${http_proxy}
    export https_proxy=${https_proxy}
    export HTTP_PROXY=${HTTP_PROXY}
    export HTTPS_PROXY=${HTTPS_PROXY}
    export PATH=${DEPOT_TOOLS}:${DEPOT_TOOLS}/${PYTHON2_PATH}:${PATH}
    export DEPOT_TOOLS_UPDATE=0
    export GCLIENT_PY3=0

    cd ${S}

    # required object files to link
    CLANG_VERSION=`ls ${CLANG_PATH}/lib/clang`
    CLANG_LIB_TARGET_PATH=${CLANG_PATH}/lib/clang/${CLANG_VERSION}/lib/${CLANG_TOOLCHAIN_TRIPLE}
    mkdir -p ${CLANG_LIB_TARGET_PATH}
    cp ${STAGING_LIBDIR}/${TARGET_SYS}/*/crtbeginS.o ${CLANG_LIB_TARGET_PATH}/
    cp ${STAGING_LIBDIR}/${TARGET_SYS}/*/crtendS.o ${CLANG_LIB_TARGET_PATH}/

    ./flutter/tools/gn ${GN_ARGS}

    echo ${ARGS_GN} >> ${ARGS_GN_FILE}
}
do_configure[depends] += "depot-tools-native:do_populate_sysroot"

do_compile() {
    export http_proxy=${http_proxy}
    export https_proxy=${https_proxy}
    export HTTP_PROXY=${HTTP_PROXY}
    export HTTPS_PROXY=${HTTPS_PROXY}
    export PATH=${DEPOT_TOOLS}:${DEPOT_TOOLS}/${PYTHON2_PATH}:${PATH}
    export DEPOT_TOOLS_UPDATE=0
    export GCLIENT_PY3=0

    cd ${S}

    autoninja -C ${OUT_DIR_REL}
}
do_compile[depends] += "depot-tools-native:do_populate_sysroot"
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    install -d ${D}${libdir}
    install -m 644 ${S}/${OUT_DIR_REL}/so.unstripped/libflutter_engine.so ${D}${libdir}

    install -d ${D}${includedir}
    install -m 644 ${S}/${OUT_DIR_REL}/flutter_embedder.h ${D}${includedir}

    install -d ${D}${datadir}/flutter
    install -m 644 ${S}/${OUT_DIR_REL}/icudtl.dat ${D}${datadir}/flutter/

    # create SDK
    install -d ${D}${datadir}/flutter/sdk
    install -d ${D}${datadir}/flutter/sdk/clang_x64
    install -m 755 ${S}/${OUT_DIR_REL}/clang_x64/gen_snapshot ${D}${datadir}/flutter/sdk/clang_x64/

    # this avoids build break when SDK tag changes in middle of build
    cd ${S}/flutter
    echo `git rev-parse HEAD` > ${D}/usr/share/flutter/sdk/engine.version
    echo ${FLUTTER_ENGINE_REPO_URL} >> ${D}/usr/share/flutter/sdk/engine.version

    cd ${D}/${datadir}/flutter
    zip -r engine_sdk.zip sdk
    rm -rf sdk
}
do_install[depends] += "zip-native:do_populate_sysroot"

PACKAGES =+ "${PN}-sdk-dev"

FILES_${PN} = "\
    ${libdir} \
    ${datadir}/flutter/icudtl.dat \
    "

FILES_${PN}-sdk-dev = "\
    ${datadir}/flutter/engine_sdk.zip \
    "

FILES_${PN}-dev = "\
    ${includedir} \
    "

# To use flutter default fonts
RDEPENDS_${PN} += " liberation-fonts"

BBCLASSEXTEND = ""
