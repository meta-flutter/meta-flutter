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
    "

SRC_URI = "gn://github.com/flutter/engine.git;name=flutter \
           file://0001-clang-toolchain.patch \
           file://0002-x64-sysroot-assert.patch \
           file://0001-remove-x11-dependency.patch \
           file://0001-prevent-redefinition-of-glib_autoptr_clear_AtkObject.patch \
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

PACKAGECONFIG ??= "release \
                   embedder-for-target \
                   prebuilt-dart-sdk \
                   fontconfig \
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


GN_ARGS = "${PACKAGECONFIG_CONFARGS} --clang --lto --no-goma --no-stripped "
GN_ARGS:append = " --target-os linux"
GN_ARGS:append = " --linux-cpu ${@gn_target_arch_name(d)}"
GN_ARGS:append = " --target-sysroot ${STAGING_DIR_TARGET}"
GN_ARGS:append = " --target-toolchain ${CLANG_PATH}"
GN_ARGS:append = " --target-triple ${CLANG_TOOLCHAIN_TRIPLE}"

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


do_unpack[network] = "1"
do_patch[network] = "1"

do_configure() {

    cd ${S}

    rm -rf fuchsia || true

    # required object files for linking
    CLANG_VERSION=`ls ${CLANG_PATH}/lib/clang`
    CLANG_LIB_TARGET_PATH=${CLANG_PATH}/lib/clang/${CLANG_VERSION}/lib/${CLANG_TOOLCHAIN_TRIPLE}
    mkdir -p ${CLANG_LIB_TARGET_PATH}
    cp ${STAGING_LIBDIR}/${TARGET_SYS}/*/crtbeginS.o ${CLANG_LIB_TARGET_PATH}/
    cp ${STAGING_LIBDIR}/${TARGET_SYS}/*/crtendS.o ${CLANG_LIB_TARGET_PATH}/
}
do_configure[depends] += "depot-tools-native:do_populate_sysroot"

do_compile() {

    FLUTTER_RUNTIME_MODES="\
        ${@d.getVarFlags("PACKAGECONFIG").get('debug')} \
        ${@d.getVarFlags("PACKAGECONFIG").get('profile')} \
        ${@d.getVarFlags("PACKAGECONFIG").get('release')} \
        ${@d.getVarFlags("PACKAGECONFIG").get('jit_release')}"

    for FLUTTER_RUNTIME_MODE in $FLUTTER_RUNTIME_MODES; do
        if [ "${FLUTTER_RUNTIME_MODE}" = "--runtime-mode" ]; then
            continue
        fi

        BUILD_DIR="$(echo ${TMP_OUT_DIR} | sed "s/_RUNTIME_/${FLUTTER_RUNTIME_MODE}/g")"

        ./flutter/tools/gn ${GN_ARGS_LESS_RUNTIME_MODES} --runtime-mode $FLUTTER_RUNTIME_MODE

        echo ${ARGS_GN} >> "${WORKDIR}/src/${BUILD_DIR}/args.gn"

        autoninja -C ${BUILD_DIR}
    done
}
do_compile[depends] += "depot-tools-native:do_populate_sysroot"
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    FLUTTER_RUNTIME_MODES="\
        ${@d.getVarFlags("PACKAGECONFIG").get('debug')} \
        ${@d.getVarFlags("PACKAGECONFIG").get('profile')} \
        ${@d.getVarFlags("PACKAGECONFIG").get('release')} \
        ${@d.getVarFlags("PACKAGECONFIG").get('jit_release')}"

    for FLUTTER_RUNTIME_MODE in $FLUTTER_RUNTIME_MODES; do
        if [ "${FLUTTER_RUNTIME_MODE}" = "--runtime-mode" ]; then
            continue
        fi

        BUILD_DIR="$(echo ${TMP_OUT_DIR} | sed "s/_RUNTIME_/${FLUTTER_RUNTIME_MODE}/g")"

        install -D -m 0644 ${S}/${BUILD_DIR}/so.unstripped/libflutter_engine.so \
            ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib/libflutter_engine.so

        if ${@bb.utils.contains('PACKAGECONFIG', 'desktop-embeddings', 'true', 'false', d)}; then
            install -D -m 0644 ${S}/${BUILD_DIR}/so.unstripped/libflutter_linux_gtk.so \
                ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/lib/libflutter_linux_gtk.so
        fi

        install -D -m 0644 ${S}/${BUILD_DIR}/flutter_embedder.h ${D}${includedir}/flutter_embedder.h

        install -D -m 0644 ${S}/${BUILD_DIR}/icudtl.dat \
            ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/data/icudtl.dat

        # create SDK
        install -D -m 0755 ${S}/${BUILD_DIR}/clang_x64/gen_snapshot \
            ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/sdk/clang_x64/gen_snapshot

        cd ${S}/flutter
        echo $SRCREV                   > ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/sdk/engine.version
        echo $FLUTTER_ENGINE_REPO_URL >> ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/sdk/engine.version
        echo $FLUTTER_SDK_VERSION     >> ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/sdk/flutter_sdk.version
        echo $RUNTIME_MODE            >> ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/sdk/flutter.runtime

        cd ${D}${datadir}/flutter/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/
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
