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

SRCREV = "4b00343963a1529598e602f0e3954c9fee9ec7d5"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "file_selector_linux_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-file-selector-file-selector-linux-file-selector-linux-example"
FLUTTER_APPLICATION_PATH = "packages/file_selector/file_selector_linux/example"
PUBSPEC_IGNORE_LOCKFILE = "1"

inherit flutter-app

RDEPENDS:${PN} += " \
    zenity \
"
