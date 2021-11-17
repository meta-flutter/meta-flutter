SUMMARY = "Embedded Linux embedding for Flutter"
DESCRIPTION = "Flutter Embedder with DRM GBM Backend."
AUTHOR = "Sony Group Corporation"
HOMEPAGE = "https://github.com/sony/flutter-embedded-linux"
BUGTRACKER = "https://github.com/sony/flutter-embedded-linux/issues"
SECTION = "graphics"
CVE_PRODUCT = "flutter-drm-gbm-backend"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d45359c88eb146940e4bede4f08c821a"

DEPENDS += "\
    compiler-rt \
    flutter-engine \
    libcxx \
    libdrm \
    libinput \
    libxkbcommon \
    virtual/egl \
    "

RDEPENDS_${PN} += "xkeyboard-config"

REQUIRED_DISTRO_FEATURES = "opengl"

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

do_install() {
   install -d ${D}${bindir}
   install -m 755 ${WORKDIR}/build/flutter-drm-gbm-backend ${D}${bindir}
}

BBCLASSEXTEND = ""
