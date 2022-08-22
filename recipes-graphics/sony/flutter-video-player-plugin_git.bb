DESCRIPTION = "Flutter Embedder with video player plugin."
CVE_PRODUCT = "libvideo_player_plugin.so"

REQUIRED_DISTRO_FEATURES = "wayland opengl"

require sony-flutter.inc

DEPENDS += "\
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    wayland \
    wayland-native \
    "

RDEPENDS:${PN} += "\
    gstreamer1.0-plugins-good \
    gstreamer1.0-plugins-bad \
    "

INSANE_SKIP:${PN} += " ldflags"
SOLIBS = ".so"
FILES_SOLIBSDEV = ""

do_install() {
    install -D -m0755 ${WORKDIR}/build/flutter-client \
        ${D}${bindir}/flutter-client
    install -D -m0644 ${WORKDIR}/build/plugins/video_player/libvideo_player_plugin.so \
        ${D}${libdir}/libvideo_player_plugin.so
}

FILES:${PN} = "\
   ${bindir} \
   ${libdir} \
   "
