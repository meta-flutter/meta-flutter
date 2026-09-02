#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "example"
DESCRIPTION = "Example for image_picker_linux implementation."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "955fd5d2d2abcebfbc34abfe56f2aad924c4d612"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-image-picker-image-picker-linux-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/image_picker/image_picker_linux/example"

inherit flutter-app
