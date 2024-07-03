#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "gma_mediation_liftoffmonetize_example"
DESCRIPTION = "Demonstrates how to use the gma_mediation_liftoffmonetize plugin."
AUTHOR = "Goolge Ads"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "ad4f3b296eb086d0cff2d71e0e253ac182abd81e"
SRC_URI = "git://github.com/googleads/googleads-mobile-flutter.git;lfs=0;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "gma_mediation_liftoffmonetize_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "googleads-flutter-packages-mediation-gma-mediation-liftoffmonetize-example"
FLUTTER_APPLICATION_PATH = "packages/mediation/gma_mediation_liftoffmonetize/example"

inherit flutter-app
