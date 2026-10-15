#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "test_plugin_example"
DESCRIPTION = "Pigeon test harness for primary plugin languages."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "8bd95a51c12c6f7e5e62d24cce5422599dd5b565"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "test_plugin_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-pigeon-platform-tests-test-plugin-test-plugin-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/pigeon/platform_tests/test_plugin/example"

inherit flutter-app
