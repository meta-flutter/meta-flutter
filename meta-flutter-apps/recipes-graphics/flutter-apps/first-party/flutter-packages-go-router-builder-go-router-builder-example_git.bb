#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "go_router_builder_example"
DESCRIPTION = "go_router_builder examples"
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "4b00343963a1529598e602f0e3954c9fee9ec7d5"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "go_router_builder_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-go-router-builder-go-router-builder-example"
FLUTTER_APPLICATION_PATH = "packages/go_router_builder/example"
PUBSPEC_IGNORE_LOCKFILE = "1"

inherit flutter-app
