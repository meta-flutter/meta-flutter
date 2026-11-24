#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "local_auth_example"
DESCRIPTION = "Demonstrates how to use the local_auth plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "76015c14934734272aa8d93d06d39d49dc3c50ab"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "local_auth_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-local-auth-local-auth-local-auth-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/local_auth/local_auth/example"

inherit flutter-app
