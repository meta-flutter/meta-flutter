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
    libcxx \
    zip-native \
    "

SRC_URI = "gn://github.com/flutter/engine.git;name=src/flutter \
           file://0001-clang-toolchain.patch \
           file://0002-x64-sysroot-assert.patch \
           file://0001-remove-x11-dependency.patch \
           file://0001-impeller-version-macro-fix.patch \
           file://0001-prevent-redefinition-of-glib_autoptr_clear_AtkObject.patch \
           "

S = "${WORKDIR}/src"

inherit gn-for-flutter python3native features_check pkgconfig

require conf/include/gn-utils.inc
require conf/include/flutter-version.inc
require conf/include/flutter-runtime.inc

BBCLASSEXTEND = "runtimerelease runtimeprofile runtimedebug"

PREFERRED_PROVIDER:${PN} = "${PN}"

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
PREFERRED_PROVIDER:libgcc = "compiler-rt"

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE:aarch64 = "(.*)"
COMPATIBLE_MACHINE:armv7 = "(.*)"
COMPATIBLE_MACHINE:armv7a = "(.*)"
COMPATIBLE_MACHINE:armv7ve = "(.*)"
COMPATIBLE_MACHINE:x86 = "(.*)"
COMPATIBLE_MACHINE:x86-64 = "(.*)"

PACKAGECONFIG ??= "embedder-for-target \
                   prebuilt-dart-sdk \
                   fontconfig \
                   ${FLUTTER_RUNTIME} \
                   ${@bb.utils.contains('DISTRO_FEATURES', 'vulkan', 'vulkan', '', d)} \
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
PACKAGECONFIG[msan] = "--msan"
PACKAGECONFIG[prebuilt-dart-sdk] = "--prebuilt-dart-sdk,--no-prebuilt-dart-sdk"
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
CLANG_PATH = "${WORKDIR}/src/buildtools/linux-x64/clang"

ARGS_GN_FILE = "${WORKDIR}/src/${OUT_DIR_REL}/args.gn"

OUT_DIR_REL = "${@get_out_dir(d)}"

GN_ARGS = "${PACKAGECONFIG_CONFARGS} --clang --lto --no-goma --no-stripped "
GN_ARGS:append = " --target-os linux"
GN_ARGS:append = " --linux-cpu ${@gn_target_arch_name(d)}"
GN_ARGS:append = " --target-sysroot ${STAGING_DIR_TARGET}"
GN_ARGS:append = " --target-toolchain ${CLANG_PATH}"
GN_ARGS:append = " --target-triple ${CLANG_TOOLCHAIN_TRIPLE}"

GN_ARGS:append:armv7 = " --arm-float-abi ${TARGET_FPU}"
GN_ARGS:append:armv7a = " --arm-float-abi ${TARGET_FPU}"
GN_ARGS:append:armv7ve = " --arm-float-abi ${TARGET_FPU}"

ARGS_GN = ""
ARGS_GN:append:aarch64 = "arm_tune = \"${@gn_get_tune_features(d)}\""
ARGS_GN:append:armv7 = "arm_tune = \"${@gn_get_tune_features(d)}\""
ARGS_GN:append:armv7a = "arm_tune = \"${@gn_get_tune_features(d)}\""
ARGS_GN:append:armv7ve = "arm_tune = \"${@gn_get_tune_features(d)}\""

do_unpack[network] = "1"
do_patch[network] = "1"

do_configure() {
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
    cd ${S}

    rm -rf fuchsia || true
    autoninja -C ${OUT_DIR_REL}
}
do_compile[network] = "0"
do_compile[depends] += "depot-tools-native:do_populate_sysroot"
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    install -D -m0644 ${S}/${OUT_DIR_REL}/so.unstripped/libflutter_engine.so \
        ${D}${libdir}/libflutter_engine.so
    
    if ${@bb.utils.contains('PACKAGECONFIG', 'desktop-embeddings', 'true', 'false', d)}; then
        install -m0644 ${S}/${OUT_DIR_REL}/so.unstripped/libflutter_linux_gtk.so ${D}${libdir}
    fi

    install -D -m0644 ${S}/${OUT_DIR_REL}/flutter_embedder.h \
        ${D}${includedir}/flutter_embedder.h

    install -D -m0644 ${S}/${OUT_DIR_REL}/icudtl.dat \
        ${D}${datadir}/flutter/icudtl.dat

    # create SDK
    install -D -m0755 ${S}/${OUT_DIR_REL}/clang_x64/gen_snapshot \
        ${D}${datadir}/flutter/sdk/clang_x64/gen_snapshot
    
    cd ${S}/flutter
    echo `git rev-parse HEAD` > ${D}${datadir}/flutter/sdk/engine.version
    echo ${FLUTTER_ENGINE_REPO_URL} >> ${D}${datadir}/flutter/sdk/engine.version
    echo ${@get_flutter_sdk_version(d)} >> ${D}${datadir}/flutter/sdk/flutter_sdk.version
    echo ${FLUTTER_RUNTIME} >> ${D}${datadir}/flutter/sdk/flutter.runtime

    cd ${D}/${datadir}/flutter
    zip -r engine_sdk.zip sdk
    rm -rf sdk
}
do_install[network] = "1"
do_install[depends] += "zip-native:do_populate_sysroot"

PACKAGES =+ "${PN}-sdk-dev"

FILES:${PN} = "\
    ${libdir} \
    ${datadir}/flutter/icudtl.dat \
    "

FILES:${PN}-sdk-dev = "\
    ${datadir}/flutter/engine_sdk.zip \
    "

FILES:${PN}-dev = "\
    ${includedir} \
    "

python () {
    d.setVar('SRCREV', gn_get_engine_commit(d))
}

RPROVIDES:${PN} = "flutter-engine-${@gn_get_flutter_runtime_name(d)}"
