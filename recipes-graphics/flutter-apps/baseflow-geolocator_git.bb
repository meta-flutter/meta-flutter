#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#
# Note this package requires OS image configuration to access geoclue (DBUS).
#

SUMMARY = "Geolocation Functional Test Case"
DESCRIPTION = "Geolocation plugin for Flutter"
AUTHOR = "Baseflow"
HOMEPAGE = "https://github.com/Baseflow/flutter-geolocator"
BUGTRACKER = "https://github.com/Baseflow/flutter-geolocator/issues"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=eb51e6812edbf587a5462bf17f2692a2"

RDEPENDS:${PN} += "geoclue"

SRCREV = "6fb962c055a19fef5437e9fba2bc935bdefa56a5"
SRC_URI = "git://github.com/Baseflow/flutter-geolocator.git;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "geolocator_linux_example"
FLUTTER_APPLICATION_PATH = "geolocator_linux/example"

inherit flutter-app
