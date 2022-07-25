DESCRIPTION = "Flutter Embedder with Wayland Client Backend."
CVE_PRODUCT = "flutter-client"

REQUIRED_DISTRO_FEATURES = "wayland opengl"

require sony-flutter.inc

DEPENDS += "\
    wayland \
    wayland-native \
    "

do_install() {
    install -D -m0755 ${WORKDIR}/build/flutter-client \
        ${D}${bindir}/flutter-client
}

FILES:${PN} = "${bindir}"
