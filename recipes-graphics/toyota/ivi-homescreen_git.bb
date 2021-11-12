SUMMARY = "Toyota IVI Homescreen"
DESCRIPTION = "Toyota's Flutter Embedder that communicates with Wayland compositor"
AUTHOR = "joel.winarske@toyotaconnected.com"
HOMEPAGE = "https://github.com/toyota-connected/ivi-homescreen"
BUGTRACKER = "https://github.com/toyota-connected/ivi-homescreen/issues"
SECTION = "graphics"
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

REQUIRED_DISTRO_FEATURES = "wayland opengl"

SRC_URI = "git://github.com/toyota-connected/ivi-homescreen.git;protocol=https;branch=main \
           file://homescreen.service \
          "

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

inherit cmake features_check

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"

EXTRA_OECMAKE += "-D CMAKE_SYSROOT=${STAGING_DIR_TARGET}/usr"

SYSTEMD_SERVICE_${PN} = "homescreen.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install_append() {
    install -D -p -m0644 ${WORKDIR}/homescreen.service ${D}${systemd_system_unitdir}/homescreen.service
}

FILES_${PN} += "${systemd_system_unitdir}"

BBCLASSEXTEND = ""
