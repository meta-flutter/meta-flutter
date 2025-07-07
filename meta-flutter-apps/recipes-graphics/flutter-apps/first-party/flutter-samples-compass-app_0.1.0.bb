#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "compass_app"
DESCRIPTION = "A sample app that helps users build and book itineraries for trips."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "74d7916e7ad985e13266daeb6691a363c390a5d4"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "compass_app"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-compass-app"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "compass_app/app"

inherit flutter-app
