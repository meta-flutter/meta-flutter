#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "gma_mediation_chartboost_example"
DESCRIPTION = "Demonstrates how to use the gma_mediation_chartboost plugin."
AUTHOR = "Goolge Ads"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "728de54c221438d1d755f5fa7ead21b7f2e242b7"
SRC_URI = "git://github.com/googleads/googleads-mobile-flutter.git;lfs=0;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "gma_mediation_chartboost_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "googleads-flutter-packages-mediation-gma-mediation-chartboost-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/mediation/gma_mediation_chartboost/example"

inherit flutter-app
