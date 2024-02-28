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

SRCREV = "3bb93366161a787b07c45d8ce98c7405b40fbf1f"
SRC_URI = "git://github.com/mogol/flutter_secure_storage.git;lfs=0;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "flutter_secure_storage_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "mogol-flutter-secure-storage-flutter-secure-storage-example"
FLUTTER_APPLICATION_PATH = "flutter_secure_storage/example"

inherit flutter-app
