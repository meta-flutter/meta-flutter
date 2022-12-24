SUMMARY = "A flutter-auto container image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

require app-container-image.bb

DISTRO_FEATURES:append = " wayland opengl systemd pam"
DISTRO_FEATURES:remove = "x11"

IMAGE_INSTALL += " \
    \
    flutter-auto-with-logging \
    \
    packagegroup-flutter-agl-apps \
    packagegroup-flutter-test-apps \
    \
    weston \
    weston-init \
    wayland-utils \
    \
    binutils \
    strace \
    ldd \
"
