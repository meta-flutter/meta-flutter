DESCRIPTION = "Flutter Engine"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://flutter/LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "9e5072f0ce81206b99db3598da687a19ce57a863"

FILESEXTRAPATHS_prepend_poky := "${THISDIR}/files:"
SRC_URI = "file://sysroot_gni.patch \
           file://custom_BUILD_gn.patch \
           file://icu.patch \
           "

S = "${WORKDIR}/git/src"

inherit python3native

DEPENDS =+ " ninja-native depot-tools-native freetype"

require gn-utils.inc

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE_aarch64 = "(.*)"
COMPATIBLE_MACHINE_armv7a = "(.*)"
COMPATIBLE_MACHINE_armv7ve = "(.*)"
COMPATIBLE_MACHINE_x86 = "(.*)"
COMPATIBLE_MACHINE_x86-64 = "(.*)"

PACKAGECONFIG ??= "embedder-for-target full-dart-sdk fontconfig skshaper stripped lto"

PACKAGECONFIG[clang] = "--clang"
PACKAGECONFIG[static-analyzer] = "--clang-static-analyzer"
PACKAGECONFIG[unoptimized] = "--unoptimized"
PACKAGECONFIG[dart-debug] = "--dart-debug"
PACKAGECONFIG[full-dart-debug] = "--full-dart-debug"
PACKAGECONFIG[full-dart-sdk] = "--full-dart-sdk"
PACKAGECONFIG[build-glfw-shell] = "--build-glfw-shell"
PACKAGECONFIG[vulkan] = "--enable-vulkan"
PACKAGECONFIG[vulkan-validation-layers] = "--enable-vulkan-validation-layers"
PACKAGECONFIG[fontconfig] = "--enable-fontconfig"
PACKAGECONFIG[skshaper] = "--enable-skshaper"
PACKAGECONFIG[embedder-for-target] = "--embedder-for-target"
PACKAGECONFIG[lto] = "--lto"
PACKAGECONFIG[stripped] = "--stripped"
PACKAGECONFIG[coverage] = "--coverage"
PACKAGECONFIG[interpreter] = "--interpreter"
PACKAGECONFIG[goma] = "--goma"
PACKAGECONFIG[asan] = "--asan"
PACKAGECONFIG[lsan] = "--lsan"
PACKAGECONFIG[msan] = "--msan"
PACKAGECONFIG[tsan] = "--tsan"
PACKAGECONFIG[ubsan] = "--ubsan"
PACKAGECONFIG[mode-debug] = "--runtime-mode debug"
PACKAGECONFIG[mode-profile] = "--runtime-mode profile"
PACKAGECONFIG[mode-release] = "--runtime-mode release"
PACKAGECONFIG[mode-jit_release] = "--runtime-mode jit_release"


GN_ARGS = " \
  ${PACKAGECONFIG_CONFARGS} \
  --target-os linux \
  --linux-cpu ${@gn_target_arch_name(d)} \
  --target-sysroot ${STAGING_DIR_TARGET} \
  --target-triple ${@gn_clang_triple_prefix(d)} \
  --target-toolchain ${S}/buildtools/linux-x64/clang \
  "

do_patch() {

    export CURL_CA_BUNDLE=${STAGING_BINDIR_NATIVE}/depot_tools/ca-certificates.crt
    export PATH=${STAGING_BINDIR_NATIVE}/depot_tools:${PATH}
    export SSH_AUTH_SOCK=${SSH_AUTH_SOCK}
    export SSH_AGENT_PID=${SSH_AGENT_PID}

    cd ${S}/..
    gclient.py config --spec 'solutions = [
        {
            "managed" : False,
            "name" : "src/flutter",
            "url" : "git@github.com:flutter/engine.git",
            "custom_vars" : {
                "download_android_deps" : False,
                "download_windows_deps" : False,
            }
        }
    ]'

    cd ${S}
    if test -f "build/config/sysroot.gni"; then
        git checkout build/config/sysroot.gni
    fi
    if test -f "build/toolchain/custom/BUILD.gn"; then
        git checkout build/toolchain/custom/BUILD.gn
    fi

    [ -d "third_party/icu" ] && cd third_party/icu
    if test -f "source/i18n/plurrule.cpp"; then
        git checkout source/i18n/plurrule.cpp
    fi

    cd ${S}
    gclient.py sync --nohooks --no-history --revision ${SRCREV} ${PARALLEL_MAKE} -v
    git apply ../../sysroot_gni.patch
    git apply ../../custom_BUILD_gn.patch

    cd third_party/icu
    git apply ../../../../icu.patch
}
do_patch[depends] =+ " \
    depot-tools-native:do_populate_sysroot \
    "

ARGS_GN_FILE = "${S}/${@get_out_dir(d)}/args.gn"

ARGS_GN_APPEND = " \
    arm_tune = \"${TUNEABI}\" \
    arm_float_abi = \"${TARGET_FPU}\" \
    "

FLUTTER_TRIPLE = "${@gn_clang_triple_prefix(d)}"


do_configure() {

    bbnote "./flutter/tools/gn ${GN_ARGS}"
    bbnote "echo ${ARGS_GN_APPEND} >> ${ARGS_GN_FILE}"

    cd ${S}

    ./flutter/tools/gn ${GN_ARGS} --disable-desktop-embeddings

    echo ${ARGS_GN_APPEND} >> ${ARGS_GN_FILE}

    # libraries required for linking so
    cp ${STAGING_LIBDIR}/${TARGET_SYS}/9.2.0/crtbeginS.o ${S}/buildtools/linux-x64/clang/lib/clang/11.0.0/lib/${FLUTTER_TRIPLE}/
    cp ${STAGING_LIBDIR}/${TARGET_SYS}/9.2.0/crtendS.o ${S}/buildtools/linux-x64/clang/lib/clang/11.0.0/lib/${FLUTTER_TRIPLE}/
}

do_compile() {

    cd ${S}
	ninja -C ${@get_out_dir(d)} -v
}
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    cd ${S}/${@get_out_dir(d)}

    install -d ${D}${bindir}
    install -d ${D}${libdir}
    install -d ${D}${includedir}
    install -m 644 icudtl.dat ${D}${bindir}
    install -m 755 libflutter_engine.so ${D}${libdir}
    install -m 644 flutter_embedder.h ${D}${includedir}
}

FILES_${PN} = " \
    ${bindir}/icudtl.dat \
    ${libdir}/libflutter_engine.so \
    "

FILES_${PN}-dev = " \
    ${includedir}/flutter_embedder.h \
    "

SYSROOT_DIRS =+ " \
    ${libdir} \
    ${includedir} \
    "

INSANE_SKIP_${PN} += "already-stripped"

# vim:set ts=4 sw=4 sts=4 expandtab:
