DESCRIPTION = "Flutter Embedder with external texture plugin."
CVE_PRODUCT = "libexternal_texture_test_plugin.so"

REQUIRED_DISTRO_FEATURES = "wayland opengl"

require sony-flutter.inc

FLUTTER_RUNTIME ??= "release"

DEPENDS += "\
    flutter-engine-${FLUTTER_RUNTIME} \
    wayland \
    wayland-native \
    "

RDEPENDS_${PN} += "\
    flutter-engine-${FLUTTER_RUNTIME} \
    "

INSANE_SKIP_${PN} += " ldflags"
SOLIBS = ".so"
FILES_SOLIBSDEV = ""

do_install() {
   install -d ${D}${bindir}
   install -d ${D}${libdir}
   install -m 755 ${WORKDIR}/build/flutter-client ${D}${bindir}
   install -m 644 ${WORKDIR}/build/plugins/external_texture_test/libexternal_texture_test_plugin.so ${D}${libdir}
}

FILES_${PN} = "\
   ${bindir} \
   ${libdir} \
   "
