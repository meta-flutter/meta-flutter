#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "firebase_ui_database_example"
DESCRIPTION = "A new Flutter project."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

<<<<<<< HEAD
SRCREV = "b59c6e9a071eb45edcf72bfd7f990e2013b2348a"
SRC_URI = "git://github.com/firebase/FirebaseUI-Flutter;lfs=0;branch=main;protocol=https;destsuffix=git"
=======
SRCREV = "0e6d85bf4c31b1a3fadff9ebdde3e00704123ad6"
SRC_URI = "git://github.com/firebase/FirebaseUI-Flutter;lfs=0;branch=main;protocol=https"
>>>>>>> 55143c1 (Flutter 3.35.7 roll)

S = "${WORKDIR}/git"

PUB_CACHE_EXTRA_ARCHIVE_PATH = "${WORKDIR}/pub_cache/bin"
PUB_CACHE_EXTRA_ARCHIVE_CMD = "flutter pub global activate melos; \
    melos bootstrap"

PUBSPEC_APPNAME = "firebase_ui_database_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "firebase-firebaseui-flutter-packages-firebase-ui-database-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/firebase_ui_database/example"

inherit flutter-app
