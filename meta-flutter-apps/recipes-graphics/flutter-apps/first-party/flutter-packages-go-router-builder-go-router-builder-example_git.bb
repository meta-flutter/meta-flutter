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
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "169d55502eca9ca5d72da960cd23e20bbcb8a20e"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "go_router_builder_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-go-router-builder-go-router-builder-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/go_router_builder/example"

inherit flutter-app
