#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "webview_flutter_android_example"
DESCRIPTION = "Demonstrates how to use the webview_flutter_android plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "8bd95a51c12c6f7e5e62d24cce5422599dd5b565"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "webview_flutter_android_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-webview-flutter-webview-flutter-android-webview-flutter-android-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/webview_flutter/webview_flutter_android/example"

inherit flutter-app
