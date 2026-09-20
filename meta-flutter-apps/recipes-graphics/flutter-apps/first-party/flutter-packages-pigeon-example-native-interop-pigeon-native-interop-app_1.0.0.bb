#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "pigeon_native_interop_app"
DESCRIPTION = "An example of using Pigeon with Native Interop in an application."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "d55f0bf635d8ded3cc0df5bb418dc672a377f83c"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "pigeon_native_interop_app"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-pigeon-example-native-interop-pigeon-native-interop-app"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/pigeon/example/native_interop_app"

inherit flutter-app
