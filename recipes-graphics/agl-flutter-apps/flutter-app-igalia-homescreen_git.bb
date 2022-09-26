SUMMARY = "Igalia Flutter Homescreen"
DESCRIPTION = "A prototype home screen for automotive grade Linux."
AUTHOR = "Igalia"
HOMEPAGE = "https://github.com/felipeerias/flutter_homescreen"
BUGTRACKER = "https://github.com/felipeerias/flutter_homescreen/issues"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

SRC_URI = "git://github.com/felipeerias/flutter_homescreen.git;protocol=https;branch=main"

SRCREV = "08bf271f7f1f14153a41005718a2090fa8e783f0"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "flutter_homescreen"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

FLUTTER_BUILD_ARGS = "bundle"

inherit flutter-app
