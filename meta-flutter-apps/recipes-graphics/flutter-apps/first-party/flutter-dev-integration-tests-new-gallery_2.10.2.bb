#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "gallery"
DESCRIPTION = "A resource to help developers evaluate and use Flutter."
AUTHOR = "Google"
HOMEPAGE = "https://github.com/flutter/flutter/dev/integration_tests/new_gallery"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1d84cf16c48e571923f837136633a265"

SRCREV = "5874a72aa4c779a02553007c47dacbefba2374dc"
SRC_URI = "git://github.com/flutter/flutter.git;lfs=1;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "gallery"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-dev-integration-tests-new-gallery"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "dev/integration_tests/new_gallery"

inherit flutter-app
