#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "webview_flutter_example"
DESCRIPTION = "Demonstrates how to use the webview_flutter plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "e4ea6bf72e1b9f47176b1a0a74669126bd0a26f4"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "webview_flutter_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-webview-flutter-webview-flutter"
FLUTTER_APPLICATION_PATH = "packages/webview_flutter/webview_flutter/example"

inherit flutter-app
