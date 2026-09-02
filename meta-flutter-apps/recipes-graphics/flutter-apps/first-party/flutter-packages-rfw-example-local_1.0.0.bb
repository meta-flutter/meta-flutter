#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "local"
DESCRIPTION = "Example of new custom local widgets for RFW"
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "955fd5d2d2abcebfbc34abfe56f2aad924c4d612"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "local"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-rfw-example-local"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/rfw/example/local"

inherit flutter-app
