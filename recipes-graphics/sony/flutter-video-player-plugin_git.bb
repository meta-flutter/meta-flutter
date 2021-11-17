SUMMARY = "Embedded Linux embedding for Flutter"
DESCRIPTION = "Flutter Embedder with video player plugin."
AUTHOR = "Sony Group Corporation"
HOMEPAGE = "https://github.com/sony/flutter-embedded-linux"
BUGTRACKER = "https://github.com/sony/flutter-embedded-linux/issues"
SECTION = "graphics"
CVE_PRODUCT = "libvideo_player_plugin.so"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d45359c88eb146940e4bede4f08c821a"

DEPENDS += "\
    compiler-rt \
    flutter-engine \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    libcxx \
    libinput \
    libxkbcommon \
    virtual/egl \
    wayland \
    wayland-native \
    "

RDEPENDS:${PN} += "\
    xkeyboard-config \
    gstreamer1.0-plugins-good \
    gstreamer1.0-plugins-bad \
    "

SRC_URI = "git://github.com/sony/flutter-embedded-linux.git;protocol=https;branch=master \
           file://0001-path-updates.patch"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

inherit pkgconfig cmake

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER:libgcc = "compiler-rt"

EXTRA_OECMAKE += "-D USER_PROJECT_PATH=${S}/examples/${PN}"

do_configure:prepend() {
   install -d ${S}/build
   ln -sf ${STAGING_LIBDIR}/libflutter_engine.so ${S}/build/libflutter_engine.so
}

INSANE_SKIP:${PN} += " ldflags"
SOLIBS = ".so"
FILES_SOLIBSDEV = ""

do_install() {
   install -d ${D}${bindir}
   install -d ${D}${libdir}
   install -m 755 ${WORKDIR}/build/flutter-client ${D}${bindir}
   install -m 644 ${WORKDIR}/build/plugins/video_player/libvideo_player_plugin.so ${D}${libdir}
}

FILES:${PN} = "\
   ${bindir} \
   ${libdir} \
   "

BBCLASSEXTEND = ""
