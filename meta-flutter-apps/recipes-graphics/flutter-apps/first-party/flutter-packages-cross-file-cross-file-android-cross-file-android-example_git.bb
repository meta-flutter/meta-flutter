#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "cross_file_android_example"
DESCRIPTION = "Demonstrates how to use the cross_file_android plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "91322aca1b857cc6cdeb5f620f7d43bb7afc42b0"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "cross_file_android_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-cross-file-cross-file-android-cross-file-android-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/cross_file/cross_file_android/example"

inherit flutter-app
