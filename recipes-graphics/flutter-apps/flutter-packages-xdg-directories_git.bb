#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "xdg_directories_example"
DESCRIPTION = "Demonstrates how to use the xdg_directories package."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "e4ea6bf72e1b9f47176b1a0a74669126bd0a26f4"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "xdg_directories_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-xdg-directories"
FLUTTER_APPLICATION_PATH = "packages/xdg_directories/example"

inherit flutter-app
