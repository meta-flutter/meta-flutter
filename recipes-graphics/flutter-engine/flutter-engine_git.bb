DESCRIPTION = "Flutter Engine"
CVE_PRODUCT = "google:flutter_engine"
LICENSE = "CLOSED"

SRC_URI = "git://github.com/flutter/engine;protocol=https;rev=f3d9f9a950eb5b115d33705922bc2ec47a2f7eb5;destsuffix=src/flutter"

S = "${WORKDIR}/git"


inherit pythonnative


REQUIRED_DISTRO_FEATURES = "wayland"

DEPENDS += "\
    curl-native \
    depot-tools-native \
    libxkbcommon \
    virtual/egl \
    wayland \
    wayland-native \
    alsa-lib \
    ${@bb.utils.contains('DISTRO_FEATURES', 'x11', 'at-spi2-core', '', d)} \
    atk \
    bison-native \
    cairo \
    dbus \
    expat \
    flac \
    freetype \
    glib-2.0 \
    gperf-native \
    gtk+3 \
    jpeg \
    libdrm \
    libxslt \
    ninja-native \
    pkgconfig-native \
    ${@bb.utils.contains('DISTRO_FEATURES', 'pulseaudio', 'pulseaudio', '', d)} \
    qemu-native \
    virtual/libgl \
"

require gn-utils.inc

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE_aarch64 = "(.*)"
COMPATIBLE_MACHINE_armv6 = "(.*)"
COMPATIBLE_MACHINE_armv7a = "(.*)"
COMPATIBLE_MACHINE_armv7ve = "(.*)"
COMPATIBLE_MACHINE_x86 = "(.*)"
COMPATIBLE_MACHINE_x86-64 = "(.*)"

# Also build the parts that are run on the host with clang.
BUILD_AR_toolchain-clang = "llvm-ar"
BUILD_CC_toolchain-clang = "clang"
BUILD_CXX_toolchain-clang = "clang++"
BUILD_LD_toolchain-clang = "clang"

PACKAGECONFIG ??= "unoptimized full-dart-sdk"

PACKAGECONFIG[clang] = "--clang"
PACKAGECONFIG[static-analyzer] = "--clang-static-analyzer"
PACKAGECONFIG[unoptimized] = "--unoptimized"
PACKAGECONFIG[dart-debug] = "--dart-debug"
PACKAGECONFIG[full-dart-debug] = "--full-dart-debug"
PACKAGECONFIG[full-dart-sdk] = "--full-dart-sdk"
PACKAGECONFIG[embedder-for-target] = "--embedder-for-target"
PACKAGECONFIG[build-glfw-shell] = "--build-glfw-shell"
PACKAGECONFIG[vulkan] = "--enable-vulkan"
PACKAGECONFIG[vulkan-validation-layers] = "--enable-vulkan-validation-layers"
PACKAGECONFIG[fontconfig] = "--enable-fontconfig"
PACKAGECONFIG[skshaper] = "--enable-skshaper"
PACKAGECONFIG[goma] = "--goma"
PACKAGECONFIG[stripped] = "--stripped"
PACKAGECONFIG[coverage] = "--coverage"
PACKAGECONFIG[interpreter] = "--interpreter"
PACKAGECONFIG[asan] = "--asan"
PACKAGECONFIG[lsan] = "--lsan"
PACKAGECONFIG[msan] = "--msan"
PACKAGECONFIG[tsan] = "--tsan"
PACKAGECONFIG[ubsan] = "--ubsan"
PACKAGECONFIG[lto] = "--lto"

#  --runtime-mode {debug,profile,release,jit_release}
#  --operator-new-alignment OPERATOR_NEW_ALIGNMENT

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
    gclient.py sync --nohooks --shallow ${PARALLEL_MAKE} -v
}
do_patch[depends] += " \
    depot-tools-native:do_populate_sysroot \
    curl-native:do_populate_sysroot \
    "

do_configure() {

    cd ${S}/src
    ./flutter/tools/gn --target-os linux --linux-cpu ${@gn_target_arch_name(d)} --target-sysroot ${STAGING_DIR_TARGET} --target-triple ${TARGET_SYS} ${PACKAGECONFIG_CONFARGS}
}

do_compile() {

    cd ${S}/src
	ninja ${PARALLEL_MAKE} -C out/linux_debug_unopt_${@gn_target_arch_name(d)}
}
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    ls -laR ${S}/src/out/linux_debug_unopt_${@gn_target_arch_name(d)}
}


# There is no need to ship empty -dev packages.
ALLOW_EMPTY_${PN}-dev = "0"


# vim:set ts=4 sw=4 sts=4 expandtab: