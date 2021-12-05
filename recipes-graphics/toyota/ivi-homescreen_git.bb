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

EXTRA_OECMAKE += "-D CMAKE_SYSROOT=${STAGING_DIR_TARGET}/usr"

SYSTEMD_SERVICE_${PN} = "homescreen.service"

do_install_append() {
    install -D -p -m0644 ${WORKDIR}/homescreen.service ${D}${systemd_system_unitdir}/homescreen.service
    
    install -d ${D}/etc/systemd/system/graphical.target.wants
    ln -sf /lib/systemd/system/homescreen.service ${D}/etc/systemd/system/graphical.target.wants/homescreen.service
}

FILES_${PN} += " \
    ${systemd_system_unitdir} \
    /etc/systemd/system \
    "

BBCLASSEXTEND = ""
