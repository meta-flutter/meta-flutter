#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "two_dimensional_examples"
DESCRIPTION = "A sample application that uses TableView and TreeView"
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "8bd95a51c12c6f7e5e62d24cce5422599dd5b565"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "two_dimensional_examples"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-two-dimensional-scrollables-two-dimensional-examples"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/two_dimensional_scrollables/example"

inherit flutter-app
