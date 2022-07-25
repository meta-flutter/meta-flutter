DESCRIPTION = "Flutter Embedder with EGLStream Backend."
CVE_PRODUCT = "flutter-drm-eglstream"

REQUIRED_DISTRO_FEATURES = "wayland opengl"

require sony-flutter.inc

DEPENDS += "libdrm"

do_install() {
    install -D -m0755 ${WORKDIR}/build/flutter-drm-eglstream-backend \
        ${D}${bindir}/flutter-drm-eglstream-backend
}

FILES:${PN} = "${bindir}"
