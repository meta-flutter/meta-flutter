SUMMARY = "An application container image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

IMAGE_FSTYPES = "container"

inherit image

IMAGE_TYPEDEP_container += "ext4"

IMAGE_FEATURES = ""
IMAGE_LINGUAS:append = " en-us"
NO_RECOMMENDATIONS = "1"

# Disable serial consoles
SERIAL_CONSOLES = ""

# We assume rngd is running on the host
VIRTUAL-RUNTIME_rngd = ""

EXTRA_IMAGEDEPENDS:remove = "arm-trusted-firmware optee-os u-boot"


IMAGE_INSTALL = " \
    base-files \
    base-passwd \
    netbase \
"

# Workaround /var/volatile for now
#ROOTFS_POSTPROCESS_COMMAND += "rootfs_fixup_var_volatile ; "

#rootfs_fixup_var_volatile () {
#    install -m 1777 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/tmp
#    install -m 755 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/log
#}
