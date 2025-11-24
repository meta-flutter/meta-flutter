#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "shared_preferences_linux_example"
DESCRIPTION = "Demonstrates how to use the shared_preferences_linux plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "fb5f02f1ebb02e6055ba9125c252fb038afabaa5"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "shared_preferences_linux_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-shared-preferences-shared-preferences-linux-shared-preferences-linux-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/shared_preferences/shared_preferences_linux/example"

inherit flutter-app
