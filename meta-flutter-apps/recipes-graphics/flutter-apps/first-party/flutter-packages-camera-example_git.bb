#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "camera_example"
DESCRIPTION = "Demonstrates how to use the camera plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "955fd5d2d2abcebfbc34abfe56f2aad924c4d612"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "camera_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-camera-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/camera/camera/example"

inherit flutter-app
