#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "firebase_ui_database_example"
DESCRIPTION = "A new Flutter project."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "c3acdba70eb6a6f79d12911361d416d9f0b866d7"
SRC_URI = "git://github.com/firebase/FirebaseUI-Flutter;lfs=0;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUB_CACHE_EXTRA_ARCHIVE_PATH = "${WORKDIR}/pub_cache/bin"
PUB_CACHE_EXTRA_ARCHIVE_CMD = "flutter pub global activate melos; \
    melos bootstrap"

PUBSPEC_APPNAME = "firebase_ui_database_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "firebase-firebaseui-flutter-packages-firebase-ui-database-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/firebase_ui_database/example"

inherit flutter-app
