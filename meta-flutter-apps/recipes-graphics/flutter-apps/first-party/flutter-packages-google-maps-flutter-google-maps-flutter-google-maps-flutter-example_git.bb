#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "google_maps_flutter_example"
DESCRIPTION = "Demonstrates how to use the google_maps_flutter plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "7632d2c7acbbd6ed1151339173dbc8dc06d64d39"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "google_maps_flutter_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-google-maps-flutter-google-maps-flutter-google-maps-flutter-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/google_maps_flutter/google_maps_flutter/example"

inherit flutter-app
