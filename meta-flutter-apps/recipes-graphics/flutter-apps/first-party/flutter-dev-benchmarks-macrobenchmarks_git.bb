#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "macrobenchmarks"
DESCRIPTION = "Performance benchmarks using flutter drive."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1d84cf16c48e571923f837136633a265"

SRCREV = "5874a72aa4c779a02553007c47dacbefba2374dc"
SRC_URI = "git://github.com/flutter/flutter.git;lfs=1;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "macrobenchmarks"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-dev-benchmarks-macrobenchmarks"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "dev/benchmarks/macrobenchmarks"

inherit flutter-app
