#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "multiplayer"
DESCRIPTION = "A game with basic multiplayer support."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "CLOSED"

SRCREV = "aa0f1b4a9572dcb6fad4258061d2033ddda7fb65"
SRC_URI = "git://github.com/flutter/games.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "multiplayer"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-games-samples-multiplayer"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "samples/multiplayer"

inherit flutter-app
