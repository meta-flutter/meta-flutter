#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "alternate_language_test_plugin_example"
DESCRIPTION = "Pigeon test harness for alternate plugin languages."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "169d55502eca9ca5d72da960cd23e20bbcb8a20e"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "alternate_language_test_plugin_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-pigeon-platform-tests-alternate-language-test-plugin-alternate-language-test-plugin-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/pigeon/platform_tests/alternate_language_test_plugin/example"

inherit flutter-app
