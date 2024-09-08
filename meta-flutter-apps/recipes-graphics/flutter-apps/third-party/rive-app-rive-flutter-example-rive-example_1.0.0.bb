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

SRCREV = "93c12806f81010363bdabf2045c0326ab1d22523"
SRC_URI = "git://github.com/rive-app/rive-flutter.git;lfs=1;branch=master;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "rive_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "rive-app-rive-flutter-example-rive-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "example"

inherit flutter-app

RDEPENDS:${PN} += " \
    rive-text \
"
