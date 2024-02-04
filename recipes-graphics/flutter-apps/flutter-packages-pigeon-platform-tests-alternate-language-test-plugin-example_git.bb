#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "alternate_language_test_plugin_example"
DESCRIPTION = "Pigeon test harness for alternate plugin languages."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "0b86a4c869fef05ccc9329a07cbea47421d7da69"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "alternate_language_test_plugin_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-pigeon-platform-tests-alternate-language-test-plugin-example"
FLUTTER_APPLICATION_PATH = "packages/pigeon/platform_tests/alternate_language_test_plugin/example"

inherit flutter-app
