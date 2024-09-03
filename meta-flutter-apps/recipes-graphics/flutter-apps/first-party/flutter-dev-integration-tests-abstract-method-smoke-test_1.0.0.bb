#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "abstract_method_smoke_test"
DESCRIPTION = "A new Flutter project."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1d84cf16c48e571923f837136633a265"

SRCREV = "cf47702e5f89592787522d5f70deff52db8628fb"
SRC_URI = "git://github.com/flutter/flutter.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "abstract_method_smoke_test"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-dev-integration-tests-abstract-method-smoke-test"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "dev/integration_tests/abstract_method_smoke_test"

inherit flutter-app
