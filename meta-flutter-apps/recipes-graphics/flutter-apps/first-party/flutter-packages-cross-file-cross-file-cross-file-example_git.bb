#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "cross_file_example"
DESCRIPTION = "Demonstrates how to use cross files."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "d55f0bf635d8ded3cc0df5bb418dc672a377f83c"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "cross_file_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-cross-file-cross-file-cross-file-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/cross_file/cross_file/example"

inherit flutter-app
