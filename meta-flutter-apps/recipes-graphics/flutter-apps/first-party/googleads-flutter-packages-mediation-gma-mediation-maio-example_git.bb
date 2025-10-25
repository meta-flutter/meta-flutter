#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "gma_mediation_maio_example"
DESCRIPTION = "Demonstrates how to use the gma_mediation_maio plugin."
AUTHOR = "Goolge Ads"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "3d614a282ff33c9ad14fc96506c71da47ae5c49f"
SRC_URI = "git://github.com/googleads/googleads-mobile-flutter.git;lfs=0;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "gma_mediation_maio_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "googleads-flutter-packages-mediation-gma-mediation-maio-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/mediation/gma_mediation_maio/example"

inherit flutter-app
