#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "A flutter-auto container image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

require app-container-image.bb

DISTRO_FEATURES:remove = " x11"
DISTRO_FEATURES:append = " vulkan wayland opengl systemd pam"

IMAGE_INSTALL += " \
    \
    vulkan-tools \
    \
    flutter-auto-verbose-logs \
    \
    weston \
    weston-init \
    \
    binutils \
    strace \
    ldd \
    gdbserver \
"
