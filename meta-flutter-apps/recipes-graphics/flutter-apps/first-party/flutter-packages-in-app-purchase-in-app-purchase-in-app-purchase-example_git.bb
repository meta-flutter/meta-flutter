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
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "1e46a6a2fb31ed8c45159d7a41fdd4ea6ea595c7"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "in_app_purchase_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-in-app-purchase-in-app-purchase-in-app-purchase-example"
FLUTTER_APPLICATION_PATH = "packages/in_app_purchase/in_app_purchase/example"

inherit flutter-app
