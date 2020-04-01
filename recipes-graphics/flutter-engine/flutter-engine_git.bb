DESCRIPTION = "Flutter Engine"

LICENSE = "CLOSED"

SRCREV = "f3d9f9a950eb5b115d33705922bc2ec47a2f7eb5"
SRC_URI = "git://github.com/flutter/engine;protocol=https;destsuffix=src/flutter"

S = "${WORKDIR}/git"


inherit pythonnative


DEPENDS += "\
    curl-native \
    ninja-native \    
    depot-tools-native \
    freetype \
"

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

    # libraries required for linking
    cp ${STAGING_LIBDIR}/${TARGET_SYS}/9.2.0/crtbeginS.o buildtools/linux-x64/clang/lib/clang/8.0.0/aarch64-unknown-linux-gnu/lib
    cp ${STAGING_LIBDIR}/${TARGET_SYS}/9.2.0/crtendS.o buildtools/linux-x64/clang/lib/clang/8.0.0/aarch64-unknown-linux-gnu/lib
}
do_patch[depends] += " \
    depot-tools-native:do_populate_sysroot \
    curl-native:do_populate_sysroot \
    "

do_configure() {

    cd ${S}/src
    ./flutter/tools/gn ${GN_ARGS}
}

do_compile() {

    cd ${S}/src
	ninja ${PARALLEL_MAKE} -C out/linux_debug_${@gn_target_arch_name(d)}
}
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {
    
    cd ${S}/src/out/linux_debug_${@gn_target_arch_name(d)}

    install -d ${D}${libdir}/flutter
    install -d ${D}${includedir}/flutter
    install -m 644 icudtl.dat ${D}${libdir}/flutter
    install -m 755 libflutter_engine.so ${D}${libdir}/flutter
    install -m 644 flutter_embedder.h ${D}${includedir}/flutter
}

FILES_${PN} = " \
    ${libdir}/flutter/* \
    "
    
FILES_${PN}-dev = " \
    ${includedir}/flutter/* \
    "

SYSROOT_DIRS += " \
    ${libdir}/flutter \
    ${includedir}/flutter \
    "

# vim:set ts=4 sw=4 sts=4 expandtab: