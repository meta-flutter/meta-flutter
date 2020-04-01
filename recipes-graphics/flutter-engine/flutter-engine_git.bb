DESCRIPTION = "Flutter Engine"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://src/flutter/LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "f3d9f9a950eb5b115d33705922bc2ec47a2f7eb5"
SRC_URI = "git://github.com/flutter/engine;protocol=https;destsuffix=src/flutter"

S = "${WORKDIR}/git"

inherit python3native

DEPENDS =+ " curl-native ninja-native depot-tools-native freetype"

require gn-utils.inc

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE_aarch64 = "(.*)"
COMPATIBLE_MACHINE_armv6 = "(.*)"
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

#  --runtime-mode {debug,profile,release,jit_release}
#  --operator-new-alignment OPERATOR_NEW_ALIGNMENT

GN_ARGS = " \
  ${PACKAGECONFIG_CONFARGS} \
  --target-os linux \
  --linux-cpu ${@gn_target_arch_name(d)} \
  --target-sysroot ${STAGING_DIR_TARGET} \
  --target-triple ${TARGET_SYS} \
  "

do_patch() {

    export CURL_CA_BUNDLE=${STAGING_BINDIR_NATIVE}/depot_tools/ca-certificates.crt
    export PATH=${STAGING_BINDIR_NATIVE}/depot_tools:${PATH}
    export SSH_AUTH_SOCK=${SSH_AUTH_SOCK}
    export SSH_AGENT_PID=${SSH_AGENT_PID}

    cd ${S}
    mkdir -p ${S}/src
    gclient.py config --spec='solutions=[{
        "managed" : False,
        "name" : "src/flutter",
        "url" : "git@github.com:flutter/engine.git",
        "custom_vars" : {
            "download_android_deps" : False,
            "download_windows_deps" : False,
        }
    }]'

    cd ${S}/src
    gclient.py sync --nohooks --no-history ${PARALLEL_MAKE} -v

}
do_patch[depends] =+ " \
    depot-tools-native:do_populate_sysroot \
    curl-native:do_populate_sysroot \
    "

do_configure() {

    cd ${S}/src
    ./flutter/tools/gn ${GN_ARGS}

    # libraries required for linking so
    mkdir -p buildtools/linux-x64/clang/lib/clang/8.0.0/aarch64-unknown-linux-gnu/lib
    cp ${STAGING_LIBDIR}/${TARGET_SYS}/9.2.0/crtbeginS.o buildtools/linux-x64/clang/lib/clang/8.0.0/aarch64-unknown-linux-gnu/lib
    cp ${STAGING_LIBDIR}/${TARGET_SYS}/9.2.0/crtendS.o buildtools/linux-x64/clang/lib/clang/8.0.0/aarch64-unknown-linux-gnu/lib
}

do_compile() {

    cd ${S}/src
	ninja ${PARALLEL_MAKE} -C ${@get_out_dir(d)}
}
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    cd ${S}/src/${@get_out_dir(d)}

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

# vim:set ts=4 sw=4 sts=4 expandtab: