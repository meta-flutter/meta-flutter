DESCRIPTION = "Flutter Embedder with DRM GBM Backend."
CVE_PRODUCT = "flutter-drm-gbm-backend"

REQUIRED_DISTRO_FEATURES = "opengl"

require sony-flutter.inc

DEPENDS += "libdrm"

do_install() {
    install -D -m0755 ${WORKDIR}/build/flutter-drm-gbm-backend \
        ${D}${bindir}/flutter-drm-gbm-backend
}

FILES:${PN} = "${bindir}"
