SUMMARY = "Embedded Linux embedding for Flutter"
DESCRIPTION = "Sony's take on existing art around Flutter on Linux."
AUTHOR = "Hidenori Matsubayashi"
HOMEPAGE = "https://github.com/sony/flutter-embedded-linux"
BUGTRACKER = "https://github.com/sony/flutter-embedded-linux/issues"
SECTION = "graphics"
CVE_PRODUCT = ""
LICENSE = "BSD-3"
LIC_FILES_CHKSUM = "file://LICENSE;md5=79ca841e7b9e09b0401186f2aa334adf"

DEPENDS += "libxkbcommon \
            libinput \
            virtual/egl \
            flutter-engine \
            wayland-native \
            wayland \
            weston \
           "

TOOLCHAIN = "clang"

SRC_URI = "git://github.com/sony/flutter-embedded-linux.git;protocol=https;branch=master \
           file://0002-weston-parser-fixes-for-desktop-vkey.patch \
           file://0003-weston-include-dir.patch"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

inherit pkgconfig cmake features_check

EXTRA_OECMAKE += "-D CMAKE_BUILD_TYPE=Debug \
                  -D USE_GLES3=ON \
                  -D DESKTOP_SHELL=ON \
                  -D USE_VIRTUAL_KEYBOARD=ON \
                  -D USER_PROJECT_PATH=${S}/examples/flutter-weston-desktop-shell-virtual-keyboard \
                  "

do_configure_prepend() {
   install -d ${S}/build
   install -m 644 ${STAGING_LIBDIR}/libflutter_engine.so ${S}/build/
}

do_install() {
   install -d ${D}${bindir}
   install -m 755 ${WORKDIR}/build/flutter-desktop-shell ${D}${bindir}
}

FILES_${PN} = "${bindir}"

BBCLASSEXTEND = ""
