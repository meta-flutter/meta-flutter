#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "tests"
DESCRIPTION = "Testing project for firebase_ui"
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "51697cc3e9c254b0198992ae680a889fdac4bdf8"
SRC_URI = "git://github.com/firebase/FirebaseUI-Flutter;lfs=0;branch=main;protocol=https"

PUBSPEC_APPNAME = "tests"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "firebase-firebaseui-flutter-tests"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "tests"

inherit flutter-app
