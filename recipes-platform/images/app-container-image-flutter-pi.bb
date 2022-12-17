SUMMARY = "A flutter-pi container image"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

require app-container-image.bb

IMAGE_INSTALL += " \
    app-container-user \
    \
    dart-sdk \
    \
    app-container-user \
    \
    packagegroup-flutter-test-apps \
    \
    flutter-pi \
"
