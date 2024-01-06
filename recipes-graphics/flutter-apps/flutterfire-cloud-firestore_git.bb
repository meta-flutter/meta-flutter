#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Cloud Firestore"
DESCRIPTION = "Cloud Firestore Functional Test Case"
AUTHOR = "Google"
HOMEPAGE = "https://github.com/firebase/flutterfire.git"
BUGTRACKER = "https://github.com/firebase/flutterfire.git/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=93a5f7c47732566fb2849f7dcddabeaf"

SRCREV = "c6341b92209d6ac80733eb7bfdbac679f44306ec"
SRC_URI = "git://github.com/firebase/flutterfire.git;lfs=0;branch=master;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "cloud_firestore_example"
FLUTTER_APPLICATION_PATH = "packages/cloud_firestore/cloud_firestore/example"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

inherit flutter-app
