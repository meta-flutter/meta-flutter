#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "cross_file_darwin_example"
DESCRIPTION = "Demonstrates how to use the cross_file_darwin plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "3258d45ac0f73c86fbe53a2e2c7b0ea4946d793c"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "cross_file_darwin_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-cross-file-cross-file-darwin-cross-file-darwin-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/cross_file/cross_file_darwin/example"

inherit flutter-app
