LICENSE = "GPL-3.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7f6b13e4480850c59e176edd427d996e"

SRCREV = "0.2.7"
SRC_URI = "git://github.com/NilsBrause/waylandpp.git"

DEPENDS_append_class-native = " pugixml-native"          
DEPENDS_append_class-target = " waylandpp-native wayland mesa"

REQUIRED_DISTRO_FEATURES = "wayland"


S = "${WORKDIR}/git"

inherit cmake


EXTRA_OECMAKE_append_class-native = " \
    -DBUILD_SCANNER=ON \
    -DBUILD_LIBRARIES=OFF \
    -DBUILD_DOCUMENTATION=OFF \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_VERBOSE_MAKEFILE=TRUE \
    "

EXTRA_OECMAKE_append_class-target = " \
    -DBUILD_SCANNER=OFF \
    -DBUILD_LIBRARIES=ON \
    -DBUILD_DOCUMENTATION=OFF \
    -DBUILD_EXAMPLES=ON \
    -DOPENGL_LIBRARY="-lEGL -lGLESv2" \
    -DOPENGL_opengl_LIBRARY=-lEGL \
    -DOPENGL_glx_LIBRARY=-lEGL \
    -DWAYLAND_SCANNERPP="${WORKDIR}/recipe-sysroot-native/${bindir}/wayland-scanner++" \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_VERBOSE_MAKEFILE=TRUE \
    -DCMAKE_EXE_LINKER_FLAGS="-Wl,--enable-new-dtags" \
    "

do_install_append_class-target() {
    install -d ${D}/${bindir}/waylandpp
    install -m 755 example/dump ${D}/${bindir}/waylandpp
    install -m 755 example/egl ${D}/${bindir}/waylandpp
    install -m 755 example/foreign_display ${D}/${bindir}/waylandpp
    install -m 755 example/proxy_wrapper ${D}/${bindir}/waylandpp
    install -m 755 example/shm ${D}/${bindir}/waylandpp
}

FILES_${PN}_append_class-native  = " \
    ${bindir}/wayland-scanner++ \
    ${libdir}/pkgconfig/wayland-scanner++.pc \
    "

FILES_${PN}_append_class-target  = " \
    ${includedir} \
    ${libdir}/lib*.so \
    ${libdir}/pkgconfig \
    ${libdir}/cmake/waylandpp \
    ${sharedir}/waylandpp/protocols \
    ${bindir}/waylandpp \
    "


BBCLASSEXTEND += "native nativesdk"
