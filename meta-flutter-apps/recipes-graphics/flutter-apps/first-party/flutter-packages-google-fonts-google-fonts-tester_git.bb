#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "google_fonts_tester"
DESCRIPTION = "A Flutter application showcasing how to use the google_fonts package."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "b7ec2d0a7f9003bf6bb2bd65f537c5700e4d3bbf"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "google_fonts_tester"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-google-fonts-google-fonts-tester"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/google_fonts/example"

inherit flutter-app
