#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Playx 3D Scene"
DESCRIPTION = "Playx 3D Scene"
AUTHOR = "Sourcya.io"
HOMEPAGE = "https://github.com/playx-flutter/playx-3d-scene"
BUGTRACKER = "https://github.com/playx-flutter/playx-3d-scene/issues"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=77f824c37447c525bd4906692858848b"

SRCREV = "7a780cc69f68f2150c8e260a74cf925fecc3764a"
SRC_URI = "git://github.com/playx-flutter/playx-3d-scene.git;lfs=0;branch=main;protocol=https;destsuffix=git \
           file://sourcya-playx-3d-scene/0001-ivi-homescreen-support.patch \
           file://sourcya-playx-3d-scene/assets/materials/textured_pbr.filamat"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "my_fox_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "sourcya-playx-3d-scene"
FLUTTER_APPLICATION_PATH = "example"

inherit flutter-app

do_install:append() {
    # Vulkan variant
    install -D -m 0644 ${WORKDIR}/sourcya-playx-3d-scene/assets/materials/textured_pbr.filamat \
        ${D}${FLUTTER_INSTALL_DIR}/${FLUTTER_SDK_VERSION}/${FLUTTER_RUNTIME_MODE}/data/flutter_assets/assets/materials/textured_pbr.filamat
}
