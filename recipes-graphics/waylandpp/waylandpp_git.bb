LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7f6b13e4480850c59e176edd427d996e"

SRC_URI = "git://github.com/NilsBrause/waylandpp.git;protocol=https"
SRCREV = "19689edf6d4b40363cc013cbf8dcf4ac10c2e9df"

DEPENDS:append:class-native = " pugixml-native"
DEPENDS:append:class-target = " waylandpp-native wayland virtual/egl"

REQUIRED_DISTRO_FEATURES = "wayland"


S = "${WORKDIR}/git"

inherit cmake


EXTRA_OECMAKE:append:class-native = " \
    -DBUILD_SCANNER=ON \
    -DBUILD_LIBRARIES=OFF \
    -DBUILD_DOCUMENTATION=OFF \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_VERBOSE_MAKEFILE=TRUE \
    "

EXTRA_OECMAKE:append:class-target = " \
    -DBUILD_SCANNER=OFF \
    -DBUILD_LIBRARIES=ON \
    -DBUILD_DOCUMENTATION=OFF \
    -DBUILD_EXAMPLES=OFF \
    -DOPENGL_LIBRARY="-lEGL -lGLESv2" \
    -DOPENGL_opengl_LIBRARY=-lEGL \
    -DOPENGL_glx_LIBRARY=-lEGL \
    -DWAYLAND_SCANNERPP="${STAGING_BINDIR_NATIVE}/wayland-scanner++" \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_VERBOSE_MAKEFILE=TRUE \
    -DCMAKE_EXE_LINKER_FLAGS="-Wl,--enable-new-dtags" \
    "

FILES:${PN} = " \
    ${libdir}/libwayland*.so* \
    "

FILES:${PN}-dev  = " \
    ${includedir}/* \
    ${libdir}/pkgconfig/* \
    ${libdir}/cmake/waylandpp/* \
    ${datadir}/waylandpp/protocols/* \
    "

FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "dev-so"

BBCLASSEXTEND += "native nativesdk"
