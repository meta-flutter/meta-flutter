#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "flutter_secure_storage_example"
DESCRIPTION = "Demonstrates how to use the flutter_secure_storage plugin."
AUTHOR = "German Saprykin"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ad4a5a1c16c771bac65521dacef3900e"

SRCREV = "20ecdf243a323417a51e21f0b8c055445645d01e"
SRC_URI = "git://github.com/mogol/flutter_secure_storage.git;lfs=0;branch=develop;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUB_CACHE_EXTRA_ARCHIVE_PATH = "${WORKDIR}/pub_cache/bin"
PUB_CACHE_EXTRA_ARCHIVE_CMD = "flutter pub global activate melos; \
    melos bootstrap"

PUBSPEC_APPNAME = "flutter_secure_storage_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "mogol-flutter-secure-storage-flutter-secure-storage-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "flutter_secure_storage/example"

inherit flutter-app
