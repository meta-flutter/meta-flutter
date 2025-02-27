#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "file_selector_linux_example"
DESCRIPTION = "Local testbed for Linux file_selector implementation."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "fec5ec563e795ace623d90857f4d1def7e86d3d8"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${UNPACKDIR}/git"

PUBSPEC_APPNAME = "file_selector_linux_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-file-selector-file-selector-linux-file-selector-linux-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/file_selector/file_selector_linux/example"

inherit flutter-app

RDEPENDS:${PN} += " \
    zenity \
"
