#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "file_selector_linux_example"
DESCRIPTION = "Local testbed for Linux file_selector implementation."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "fb5f02f1ebb02e6055ba9125c252fb038afabaa5"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "file_selector_linux_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-file-selector-file-selector-linux-file-selector-linux-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/file_selector/file_selector_linux/example"

inherit flutter-app

RDEPENDS:${PN} += " \
    zenity \
"
