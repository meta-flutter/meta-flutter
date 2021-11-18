SUMMARY = "Embedded Linux embedding for Flutter"
DESCRIPTION = "Flutter Embedder with external texture plugin."
AUTHOR = "Sony Group Corporation"
HOMEPAGE = "https://github.com/sony/flutter-embedded-linux"
BUGTRACKER = "https://github.com/sony/flutter-embedded-linux/issues"
SECTION = "graphics"
CVE_PRODUCT = "libexternal_texture_test_plugin.so"
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

RDEPENDS_${PN} += "xkeyboard-config"

REQUIRED_DISTRO_FEATURES = "wayland"

SRC_URI = "git://github.com/sony/flutter-embedded-linux.git;protocol=https;branch=master \
           file://0001-path-updates.patch"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

inherit pkgconfig cmake features_check

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"

EXTRA_OECMAKE += "-D USER_PROJECT_PATH=${S}/examples/${PN}"

do_configure_prepend() {
   install -d ${S}/build
   ln -sf ${STAGING_LIBDIR}/libflutter_engine.so ${S}/build/libflutter_engine.so
}

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

BBCLASSEXTEND = ""
