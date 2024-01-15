#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Secure Storage"
DESCRIPTION = "Secure Storage Functional Test Case"
AUTHOR = "German Saprykin"
HOMEPAGE = "https://github.com/mogol/flutter_secure_storage"
BUGTRACKER = "https://github.com/mogol/flutter_secure_storage/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ad4a5a1c16c771bac65521dacef3900e"

SRCREV = "57c2b54c3e1a839d1f6754a487b85b44e027b46d"
SRC_URI = "git://github.com/mogol/flutter_secure_storage.git;lfs=0;branch=develop;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "flutter_secure_storage_example"
FLUTTER_APPLICATION_PATH = "flutter_secure_storage/example"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

inherit flutter-app