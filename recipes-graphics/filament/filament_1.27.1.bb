SUMMARY = "Lightweight 3D Render Engine"
DESCRIPTION = "Filament is a real-time physically based rendering engine for Android, iOS, Windows, Linux, macOS, and WebGL2"
AUTHOR = "Filament Authors"
HOMEPAGE = "https://github.com/google/filament"
BUGTRACKER = "https://github.com/google/filament/issues"
SECTION = "graphics"
CVE_PRODUCT = ""
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

DEPENDS += "\
    compiler-rt \
    libcxx \
    "

DEPENDS_class-target += "\
    filament-native \
    "

PV .= "+${SRCPV}"

SRC_URI = "git://github.com/google/filament;protocol=https;nobranch=1 \
           file://ImportExecutables-Release.cmake"

SRCREV = "867d4d44f5ef2a56f663f5d8a7ba984407adfcbe"

S = "${WORKDIR}/git"

RUNTIME = "llvm"
TOOLCHAIN = "clang"
TOOLCHAIN_class-native = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"

PACKAGECONFIG ??= "${@bb.utils.filter('DISTRO_FEATURES', 'wayland x11 opengl vulkan', d)} shared samples"

PACKAGECONFIG[opengl] = "-DFILAMENT_SUPPORTS_OPENGL=ON, -DFILAMENT_SUPPORTS_OPENGL=OFF, virtual/egl"
PACKAGECONFIG[vulkan] = "-DFILAMENT_SUPPORTS_VULKAN=ON, -DFILAMENT_SUPPORTS_VULKAN=OFF, vulkan-loader"
PACKAGECONFIG[wayland] = "-DFILAMENT_SUPPORTS_WAYLAND=ON, -DFILAMENT_SUPPORTS_WAYLAND=OFF, wayland wayland-native wayland-protocols"
PACKAGECONFIG[x11] = "-DFILAMENT_SUPPORTS_X11=ON, -DFILAMENT_SUPPORTS_X11=OFF, libxcb libx11 libxrandr"
PACKAGECONFIG[shared] = "-DBUILD_SHARED_LIBS=ON, -DBUILD_SHARED_LIBS=OFF"
PACKAGECONFIG_class-target[samples] = "-DFILAMENT_SKIP_SAMPLES=OFF, -DFILAMENT_SKIP_SAMPLES=ON"

inherit cmake pkgconfig

EXTRA_OECMAKE_class-native += " \
    -D FILAMENT_BUILD_FILAMAT=OFF \
    -D FILAMENT_SKIP_SAMPLES=ON \
    -D FILAMENT_SKIP_SDL2=ON \
    ${PACKAGECONFIG_CONFARGS} \
    "

EXTRA_OECMAKE_class-target += " \
    -D FILAMENT_LINUX_IS_MOBILE=ON \
    -D IMPORT_EXECUTABLES_DIR=. \
    -D FILAMENT_SKIP_SDL2=ON \
    -D DIST_ARCH=${BUILD_ARCH} \
    -D FILAMENT_HOST_TOOLS_ROOT=${STAGING_BINDIR_NATIVE} \
    ${PACKAGECONFIG_CONFARGS} \
    "

do_configure_prepend_class-target () {
    cp ${WORKDIR}/ImportExecutables-Release.cmake ${S}
}

do_install_append_class-native () {
    rm -rf ${D}${libdir}
    rm -rf ${D}${includedir}

    ls -la ${D}
}

do_install_append_class-target () {
    ls -la ${D}

    mv ${D}${libdir}/*/*.a ${D}${libdir}
    rm -rf ${D}${libdir}/${BUILD_ARCH}
    rm ${D}/usr/LICENSE
    rm ${D}/usr/README.md
}


BBCLASSEXTEND += "native nativesdk"
