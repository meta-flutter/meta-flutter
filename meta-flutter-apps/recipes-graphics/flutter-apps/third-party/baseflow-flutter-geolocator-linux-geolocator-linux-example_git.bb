#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "geolocator_linux_example"
DESCRIPTION = "Demonstrates how to use the geolocator_linux plugin."
AUTHOR = "baseflow"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=eb51e6812edbf587a5462bf17f2692a2"

SRCREV = "951162a8698b77c33cbb86c54165ec6daf980014"
SRC_URI = "git://github.com/Baseflow/flutter-geolocator.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "geolocator_linux_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "baseflow-flutter-geolocator-linux-geolocator-linux-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "geolocator_linux/example"

inherit flutter-app

RDEPENDS:${PN} += " \
    geoclue \
"
