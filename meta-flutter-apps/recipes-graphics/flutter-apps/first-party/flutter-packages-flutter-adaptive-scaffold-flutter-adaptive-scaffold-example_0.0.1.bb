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

SRCREV = "1d00782b90d4c57f03c7b6b1b4e4f91d39a0426b"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "flutter_adaptive_scaffold_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-flutter-adaptive-scaffold-flutter-adaptive-scaffold-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/flutter_adaptive_scaffold/example"

inherit flutter-app
