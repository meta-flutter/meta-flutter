#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "google_mobile_ads_example"
DESCRIPTION = "Demonstrates how to use the google mobile ads plugin."
AUTHOR = "Goolge Ads"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "9ec68e84a2d3a0fc7b14c0a4885d15e709575d7a"
SRC_URI = "git://github.com/googleads/googleads-mobile-flutter.git;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "google_mobile_ads_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "googleads-flutter-packages-google-mobile-ads-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/google_mobile_ads/example"

inherit flutter-app
