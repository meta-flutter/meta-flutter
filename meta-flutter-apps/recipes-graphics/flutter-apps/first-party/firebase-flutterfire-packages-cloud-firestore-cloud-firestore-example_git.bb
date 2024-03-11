#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "cloud_firestore_example"
DESCRIPTION = "Demonstrates how to use the firestore plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=93a5f7c47732566fb2849f7dcddabeaf"

SRCREV = "f5c4354a66377da9d231c5e3fc7e955ddb7ef8cf"
SRC_URI = "git://github.com/firebase/flutterfire.git;lfs=0;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUB_CACHE_EXTRA_ARCHIVE_PATH = "${WORKDIR}/pub_cache/bin"
PUB_CACHE_EXTRA_ARCHIVE_CMD = "flutter pub global activate melos; \
    melos bootstrap"

PUBSPEC_APPNAME = "cloud_firestore_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "firebase-flutterfire-packages-cloud-firestore-cloud-firestore-example"
FLUTTER_APPLICATION_PATH = "packages/cloud_firestore/cloud_firestore/example"

inherit flutter-app
