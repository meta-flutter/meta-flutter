#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "pointer_interceptor_example"
DESCRIPTION = "An example app for the pointer_interceptor package."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "fb5f02f1ebb02e6055ba9125c252fb038afabaa5"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "pointer_interceptor_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-pointer-interceptor-pointer-interceptor-pointer-interceptor-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/pointer_interceptor/pointer_interceptor/example"

inherit flutter-app
