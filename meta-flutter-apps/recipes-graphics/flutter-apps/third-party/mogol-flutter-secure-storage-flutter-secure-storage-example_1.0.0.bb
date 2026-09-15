#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "flutter_secure_storage_example"
DESCRIPTION = "Demonstrates how to use the flutter_secure_storage plugin."
AUTHOR = "German Saprykin"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ad4a5a1c16c771bac65521dacef3900e"

SRCREV = "7ee0d6733b3e5179f6874eff34f6d9756c44c16e"
SRC_URI = "git://github.com/mogol/flutter_secure_storage.git;lfs=0;branch=develop;protocol=https"

PUBSPEC_APPNAME = "flutter_secure_storage_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "mogol-flutter-secure-storage-flutter-secure-storage-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "flutter_secure_storage/example"

inherit flutter-app
