#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "google_maps_flutter_web_integration_tests"
DESCRIPTION = "None"
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "b33d9334f58b1cb6dfc1d6a46deb1c24f4a6425a"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "google_maps_flutter_web_integration_tests"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-google-maps-flutter-google-maps-flutter-web-example-google-maps-flutter-web-integration-tests"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/google_maps_flutter/google_maps_flutter_web/example/latest"

inherit flutter-app
