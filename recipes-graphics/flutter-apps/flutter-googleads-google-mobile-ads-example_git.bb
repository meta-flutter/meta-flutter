#
# Copyright (c) 2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Google Mobile Ads Example"
DESCRIPTION = "Demonstrates how to use the google mobile ads plugin."
AUTHOR = "Googleads Team"
HOMEPAGE = "https://github.com/googleads/googleads-mobile-flutter"
BUGTRACKER = "https://github.com/googleads/googleads-mobile-flutter/issues"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://packages/google_mobile_ads/LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "31c045eb153ba320050b1c439f7343a1bda08d94"
SRC_URI = "git://github.com/googleads/googleads-mobile-flutter.git;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "google_mobile_ads_example"
FLUTTER_APPLICATION_PATH = "packages/google_mobile_ads/example"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

inherit flutter-app
