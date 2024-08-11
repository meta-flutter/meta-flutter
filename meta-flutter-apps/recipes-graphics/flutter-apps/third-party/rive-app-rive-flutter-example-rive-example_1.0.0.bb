#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "rive_example"
DESCRIPTION = "A collection of Rive Flutter examples"
AUTHOR = "Rive"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c52243a14a066c83e50525d9ad046678"

SRCREV = "3dbef04b8b3e1832055a4a2c6d092c2006d220eb"
SRC_URI = "git://github.com/rive-app/rive-flutter.git;lfs=1;branch=master;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "rive_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "rive-app-rive-flutter-example-rive-example"
FLUTTER_APPLICATION_PATH = "example"
PUBSPEC_IGNORE_LOCKFILE = "1"

inherit flutter-app

RDEPENDS:${PN} += " \
    rive-text \
"
