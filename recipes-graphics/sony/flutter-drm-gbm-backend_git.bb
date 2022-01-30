DESCRIPTION = "Flutter Embedder with DRM GBM Backend."
CVE_PRODUCT = "flutter-drm-gbm-backend"

REQUIRED_DISTRO_FEATURES = "opengl"

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
   install -m 755 ${WORKDIR}/build/flutter-drm-gbm-backend ${D}${bindir}
}

FILES_${PN} = "\
   ${bindir} \
   "
