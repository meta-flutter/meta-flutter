#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "test_egl"
DESCRIPTION = "A new Flutter project."
AUTHOR = "Misc"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=df6bd2163489eedcdea6b9406bcbe1dd"

SRCREV = "c92bed290b4f696e8434cc0af4af020420e518bc"
SRC_URI = "git://github.com/meta-flutter/tests;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "test_egl"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "meta-flutter-tests-textures-test-egl"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "textures/test_egl"

inherit flutter-app
