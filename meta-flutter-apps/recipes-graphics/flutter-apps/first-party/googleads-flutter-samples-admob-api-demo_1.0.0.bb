#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "api_demo"
DESCRIPTION = "A Flutter project showcasing the google_mobile_ads Flutter plugin and its APIs in a demo app."
AUTHOR = "Goolge Ads"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "3a67f7a08c004eff3705d73e8f3b004cf4cbad6a"
SRC_URI = "git://github.com/googleads/googleads-mobile-flutter.git;lfs=0;branch=main;protocol=https"

PUBSPEC_APPNAME = "api_demo"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "googleads-flutter-samples-admob-api-demo"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "samples/admob/api_demo"

# The git fetcher unpacks to ${WORKDIR}/git on this release, while the default
# S is ${WORKDIR}/${BP}. Newer oe-core lines them up with
# BB_GIT_DEFAULT_DESTSUFFIX, which bitbake does not have here, so S is explicit.
S = "${WORKDIR}/git"

inherit flutter-app
