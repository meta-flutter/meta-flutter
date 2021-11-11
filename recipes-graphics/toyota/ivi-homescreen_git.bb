SUMMARY = "Toyota IVI Homescreen"
DESCRIPTION = "Toyota's Flutter Embedder that communicates with Wayland compositor"
AUTHOR = "joel.winarske@toyotaconnected.com"
HOMEPAGE = "https://github.com/toyota-connected/ivi-homescreen"
BUGTRACKER = "https://github.com/toyota-connected/ivi-homescreen/issues"
SECTION = "connectivity"
CVE_PRODUCT = "homescreen"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=39ae29158ce710399736340c60147314"

DEPENDS += "\
    compiler-rt \
    flutter-engine \
    libcxx \
    virtual/egl \
    wayland \
    wayland-native \
    wayland-protocols \
    "

PV .= "+${SRCPV}"

REQUIRED_DISTRO_FEATURES = "wayland opengl"

SRC_URI = "git://github.com/toyota-connected/ivi-homescreen.git;protocol=https;branch=main"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

inherit cmake features_check

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER:libgcc = "compiler-rt"

EXTRA_OECMAKE += "-D CMAKE_SYSROOT=${STAGING_DIR_TARGET}/usr"


BBCLASSEXTEND = ""
