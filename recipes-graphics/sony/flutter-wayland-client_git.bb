SUMMARY = "Embedded Linux embedding for Flutter"
DESCRIPTION = "Flutter Embedder with Wayland Client Backend."
AUTHOR = "Hidenori Matsubayashi"
HOMEPAGE = "https://github.com/sony/flutter-embedded-linux"
BUGTRACKER = "https://github.com/sony/flutter-embedded-linux/issues"
SECTION = "graphics"
CVE_PRODUCT = "flutter-client"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d45359c88eb146940e4bede4f08c821a"

DEPENDS += "\
    compiler-rt \
    flutter-engine \
    libcxx \
    libinput \
    libxkbcommon \
    virtual/egl \
    wayland \
    wayland-native \
    "

RDEPENDS:${PN} += "xkeyboard-config"

REQUIRED_DISTRO_FEATURES = "wayland opengl"

SRC_URI = "git://github.com/sony/flutter-embedded-linux.git;protocol=https;branch=master"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

inherit pkgconfig cmake features_check

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER:libgcc = "compiler-rt"

EXTRA_OECMAKE += "-D USER_PROJECT_PATH=examples/${PN}"

do_configure:prepend() {
   install -d ${S}/build
   ln -sf ${STAGING_LIBDIR}/libflutter_engine.so ${S}/build/libflutter_engine.so
}

do_install() {
   install -d ${D}${bindir}
   install -m 755 ${WORKDIR}/build/flutter-client ${D}${bindir}
}

BBCLASSEXTEND = ""
