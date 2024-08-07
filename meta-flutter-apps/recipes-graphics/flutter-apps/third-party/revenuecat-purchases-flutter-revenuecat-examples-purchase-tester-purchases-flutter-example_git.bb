#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "purchases_flutter_example"
DESCRIPTION = "Demonstrates how to use the purchases_flutter plugin."
AUTHOR = "RevenueCat"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c582549082bd8c4f8e574bd29c5212ed"

SRCREV = "2af414435e75e4b7c1959d80d4552cf9e249f0e1"
SRC_URI = " \
    git://github.com/RevenueCat/purchases-flutter.git;lfs=0;nobranch=1;protocol=https;destsuffix=git \
    file://purchases-flutter/0001-generic-Linux.patch \
"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "purchases_flutter_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "revenuecat-purchases-flutter-revenuecat-examples-purchase-tester-purchases-flutter-example"
FLUTTER_APPLICATION_PATH = "revenuecat_examples/purchase_tester"

inherit flutter-app
