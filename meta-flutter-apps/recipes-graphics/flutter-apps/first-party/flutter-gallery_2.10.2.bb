#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "gallery"
DESCRIPTION = "A resource to help developers evaluate and use Flutter."
AUTHOR = "Google"
HOMEPAGE = "https://github.com/flutter/gallery"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3ac21e3d8ebe7dd79f273ca11b9e7b4e"

SRCREV = "5874a72aa4c779a02553007c47dacbefba2374dc"
SRC_URI = " \
    git://github.com/flutter/flutter.git;lfs=0;branch=main;protocol=https;destsuffix=git \
"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "gallery"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter/dev/integration_tests/flutter_gallery"
FLUTTER_APPLICATION_PATH = ""

inherit flutter-app

RDEPENDS:${PN} += " \
    xdg-user-dirs \
"
