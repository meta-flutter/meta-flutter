DESCRIPTION = "Flutter Embedder with external texture plugin."
CVE_PRODUCT = "libexternal_texture_test_plugin.so"

REQUIRED_DISTRO_FEATURES = "wayland opengl"

require sony-flutter.inc

DEPENDS += "\
    wayland \
    wayland-native \
    "

INSANE_SKIP:${PN} += " ldflags"
SOLIBS = ".so"
FILES_SOLIBSDEV = ""

do_install() {
    install -D -m0755 ${WORKDIR}/build/flutter-client \
        ${D}${bindir}/flutter-client
    install -D -m0644 ${WORKDIR}/build/plugins/external_texture_test/libexternal_texture_test_plugin.so \
        ${D}${libdir}/libexternal_texture_test_plugin.so
}

FILES:${PN} = "\
   ${bindir} \
   ${libdir} \
   "
