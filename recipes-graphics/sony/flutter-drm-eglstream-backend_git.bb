DESCRIPTION = "Flutter Embedder with EGLStream Backend."
CVE_PRODUCT = "flutter-drm-eglstream"

REQUIRED_DISTRO_FEATURES = "wayland opengl"

require sony-flutter.inc

DEPENDS += "\
    libdrm \
    "

do_install() {
   install -d ${D}${bindir}
   install -m 755 ${WORKDIR}/build/flutter-drm-eglstream-backend ${D}${bindir}
}

FILES:${PN} = "\
   ${bindir} \
   "
