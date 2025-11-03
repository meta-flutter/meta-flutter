#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "in_app_purchase_example"
DESCRIPTION = "Demonstrates how to use the in_app_purchase plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "5fc2fd7169cfe841bb46f0f825fc236d2c4c206f"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "in_app_purchase_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-in-app-purchase-in-app-purchase-in-app-purchase-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/in_app_purchase/in_app_purchase/example"

inherit flutter-app
