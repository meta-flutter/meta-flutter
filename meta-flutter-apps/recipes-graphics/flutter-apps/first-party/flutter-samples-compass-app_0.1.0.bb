#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "compass_app"
DESCRIPTION = "A new Flutter project."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "9a01200cf85b77e78f0d098dd5157e10f8a03247"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "compass_app"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-compass-app"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "compass_app/app"

inherit flutter-app
