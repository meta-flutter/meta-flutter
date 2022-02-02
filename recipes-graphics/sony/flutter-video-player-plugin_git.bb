DESCRIPTION = "Flutter Embedder with video player plugin."
CVE_PRODUCT = "libvideo_player_plugin.so"

REQUIRED_DISTRO_FEATURES = "wayland opengl"

require sony-flutter.inc

FLUTTER_RUNTIME ??= "release"

DEPENDS += "\
    flutter-engine-${FLUTTER_RUNTIME} \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    wayland \
    wayland-native \
    "

RDEPENDS_${PN} += "\
    flutter-engine-${FLUTTER_RUNTIME} \
    gstreamer1.0-plugins-good \
    gstreamer1.0-plugins-bad \
    "

INSANE_SKIP_${PN} += " ldflags"
SOLIBS = ".so"
FILES_SOLIBSDEV = ""

do_install() {
   install -d ${D}${bindir}
   install -d ${D}${libdir}
   install -m 755 ${WORKDIR}/build/flutter-client ${D}${bindir}
   install -m 644 ${WORKDIR}/build/plugins/video_player/libvideo_player_plugin.so ${D}${libdir}
}

FILES_${PN} = "\
   ${bindir} \
   ${libdir} \
   "
