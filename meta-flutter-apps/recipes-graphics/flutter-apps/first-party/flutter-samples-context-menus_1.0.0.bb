#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "context_menus"
DESCRIPTION = "A new Flutter project."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "4d67572868a5c36bb2c4c22c13c8002be019c365"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "context_menus"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-context-menus"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "context_menus"

inherit flutter-app
