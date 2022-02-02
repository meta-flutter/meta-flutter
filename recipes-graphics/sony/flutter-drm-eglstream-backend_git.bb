DESCRIPTION = "Flutter Embedder with EGLStream Backend."
CVE_PRODUCT = "flutter-drm-eglstream"

REQUIRED_DISTRO_FEATURES = "wayland opengl"

require sony-flutter.inc

FLUTTER_RUNTIME ??= "release"

DEPENDS += "\
    libdrm \
    flutter-engine-${FLUTTER_RUNTIME} \
    "

RDEPENDS_${PN} += "\
    flutter-engine-${FLUTTER_RUNTIME} \
    "

do_install() {
   install -d ${D}${bindir}
   install -m 755 ${WORKDIR}/build/flutter-drm-eglstream-backend ${D}${bindir}
}

FILES_${PN} = "\
   ${bindir} \
   "
