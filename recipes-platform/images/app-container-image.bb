SUMMARY = "An application container image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

IMAGE_FSTYPES = "container"

inherit image

IMAGE_TYPEDEP_container += "ext4"

IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""
NO_RECOMMENDATIONS = "1"

# Disable serial consoles
SERIAL_CONSOLES = ""

# We assume rngd is running on the host
VIRTUAL-RUNTIME:rngd = ""

EXTRA_IMAGEDEPENDS:remove = "arm-trusted-firmware optee-os u-boot"
