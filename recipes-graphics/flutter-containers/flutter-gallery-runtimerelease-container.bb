SUMMARY = "Flutter Gallery Container image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"
IMAGE_FSTYPES = "container"

inherit image

IMAGE_TYPEDEP_container += "ext4"
IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""

NO_RECOMMENDATIONS = "1"

IMAGE_INSTALL = " \
  base-files \
  base-passwd \
  netbase \
  flutter-auto-runtimerelease \
  flutter-gallery-runtimerelease \
"

# Workaround /var/volatile for now
ROOTFS_POSTPROCESS_COMMAND += "rootfs_fixup_var_volatile ; "

rootfs_fixup_var_volatile () {
  install -m 1777 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/tmp
  install -m 755 -d ${IMAGE_ROOTFS}/${localstatedir}/volatile/log
}
