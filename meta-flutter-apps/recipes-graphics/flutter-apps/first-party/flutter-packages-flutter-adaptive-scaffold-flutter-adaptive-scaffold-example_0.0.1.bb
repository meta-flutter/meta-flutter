#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "flutter_adaptive_scaffold_example"
DESCRIPTION = "Multiple examples of the usage of the AdaptiveScaffold widget and its lower level widgets."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "fec5ec563e795ace623d90857f4d1def7e86d3d8"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "flutter_adaptive_scaffold_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-flutter-adaptive-scaffold-flutter-adaptive-scaffold-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/flutter_adaptive_scaffold/example"

inherit flutter-app
