DESCRIPTION = "Flutter Embedder with X11 Backend."
CVE_PRODUCT = "flutter-x11-client"

REQUIRED_DISTRO_FEATURES = "x11 opengl"

require sony-flutter.inc

do_install() {
   install -d ${D}${bindir}
   install -m 755 ${WORKDIR}/build/flutter-x11-client ${D}${bindir}
}

FILES:${PN} = "\
   ${bindir} \
   "
