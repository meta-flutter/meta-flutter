#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "pathproviderexample"
DESCRIPTION = "Demonstrates how to use the path_provider_linux plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "91322aca1b857cc6cdeb5f620f7d43bb7afc42b0"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "pathproviderexample"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-path-provider-path-provider-linux-pathproviderexample"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/path_provider/path_provider_linux/example"

inherit flutter-app
