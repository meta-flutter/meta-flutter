#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "A flutter-auto container image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

require app-container-image.bb

DISTRO_FEATURES_remove = " x11"
DISTRO_FEATURES_append = " vulkan wayland opengl systemd pam"

IMAGE_INSTALL += " \
    \
    vulkan-tools \
    \
    flutter-auto-with-logging \
    \
    packagegroup-flutter-test-apps \
    \
    weston \
    weston-init \
    \
    binutils \
    strace \
    ldd \
    gdbserver \
"
