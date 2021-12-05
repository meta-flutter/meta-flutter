SUMMARY = "Toyota IVI Homescreen"
DESCRIPTION = "Toyota's Flutter Embedder that communicates with AGL-compositor/Wayland compositors"
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
SRCREV = "4a3d34fb4e1e4c1cffba11a0ce7f031998f48fb5"

S = "${WORKDIR}/git"

inherit cmake features_check systemd

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"

EXTRA_OECMAKE += "\
    -D CMAKE_SYSROOT=${STAGING_DIR_TARGET}/usr \
    "

PACKAGECONFIG ??= "${@bb.utils.filter('DISTRO_FEATURES', 'systemd', d)}"

do_install_append() {
	if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -d ${D}${systemd_system_unitdir}
        install -m 644 ${WORKDIR}/homescreen.service ${D}${systemd_system_unitdir}
	fi
}

SYSTEMD_SERVICE_${PN} += "${@bb.utils.contains('PACKAGECONFIG', 'systemd', 'homescreen.service', '', d)}"
SYSTEMD_PACKAGES = "${@bb.utils.contains('PACKAGECONFIG', 'systemd', '${PN}', '', d)}"
