#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "gma_mediation_ironsource_example"
DESCRIPTION = "Demonstrates how to use the gma_mediation_ironsource plugin."
AUTHOR = "Goolge Ads"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "310463dc43be19f80cb9d792a08d9058c8a7a665"
SRC_URI = "git://github.com/googleads/googleads-mobile-flutter.git;lfs=0;branch=main;protocol=https"

PUBSPEC_APPNAME = "gma_mediation_ironsource_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "googleads-flutter-packages-mediation-gma-mediation-ironsource-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/mediation/gma_mediation_ironsource/example"

inherit flutter-app
