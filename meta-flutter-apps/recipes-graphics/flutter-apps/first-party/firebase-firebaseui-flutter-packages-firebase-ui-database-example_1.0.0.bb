#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "firebase_ui_database_example"
DESCRIPTION = "A new Flutter project."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "84da53c391d3f79926ae701c59c67b6037308e0b"
SRC_URI = "git://github.com/firebase/FirebaseUI-Flutter;lfs=0;branch=main;protocol=https"

PUBSPEC_APPNAME = "firebase_ui_database_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "firebase-firebaseui-flutter-packages-firebase-ui-database-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/firebase_ui_database/example"

inherit flutter-app
