#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Application container image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

IMAGE_FSTYPES = "container"

inherit image

IMAGE_TYPEDEP_container += "ext4"

IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""
NO_RECOMMENDATIONS = "1"

IMAGE_INSTALL = " \
    mesa-megadriver \
    base-files \
    base-passwd \
    netbase \
    ${CONTAINER_SHELL} \
"

# This is required, since there are postinstall scripts in base-files and base-passwd
# that reference /bin/sh and we'll get a rootfs error if there's no shell or no dummy
# provider.
CONTAINER_SHELL ?= "${@bb.utils.contains('PACKAGE_EXTRA_ARCHS', 'container-dummy-provides', 'container-dummy-provides', 'busybox', d)}"

# Disable serial consoles
SERIAL_CONSOLES = ""

# We assume rngd is running on the host
VIRTUAL-RUNTIME:rngd = ""

EXTRA_IMAGEDEPENDS:remove = "arm-trusted-firmware optee-os u-boot"
