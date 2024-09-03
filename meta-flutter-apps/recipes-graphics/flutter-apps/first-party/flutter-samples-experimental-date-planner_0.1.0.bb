#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "date_planner"
DESCRIPTION = "An iOS-style date planner."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "b6e58325bf7d0863055310cd23d052ac23943871"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "date_planner"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-experimental-date-planner"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "experimental/date_planner"

inherit flutter-app
