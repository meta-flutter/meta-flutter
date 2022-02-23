DESCRIPTION = "Flutter Embedder with Wayland Client Backend."
CVE_PRODUCT = "flutter-client"

REQUIRED_DISTRO_FEATURES = "wayland opengl"

require sony-flutter.inc

FLUTTER_RUNTIME ??= "release"

DEPENDS += "\
    flutter-engine-${FLUTTER_RUNTIME} \
    wayland \
    wayland-native \
    "

RDEPENDS:${PN} += "\
    flutter-engine-${FLUTTER_RUNTIME} \
    "

do_install() {
   install -d ${D}${bindir}
   install -m 755 ${WORKDIR}/build/flutter-client ${D}${bindir}
}

FILES:${PN} = "\
   ${bindir} \
   "
