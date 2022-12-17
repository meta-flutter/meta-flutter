SUMMARY = "A flutter-auto container image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

require app-container-image.bb

IMAGE_INSTALL += " \
    binutils \
    dart-sdk \
    git \
    ldd \
    strace \
"
