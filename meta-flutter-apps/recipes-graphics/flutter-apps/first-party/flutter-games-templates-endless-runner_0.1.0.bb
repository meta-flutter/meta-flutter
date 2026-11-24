#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "endless_runner"
DESCRIPTION = "A Flame game template built with Flutter."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "CLOSED"

SRCREV = "e3ba3c566e332c6f6c0dd54ce2a6306b7abf114b"
SRC_URI = "git://github.com/flutter/games.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "endless_runner"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-games-templates-endless-runner"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "templates/endless_runner"

inherit flutter-app
