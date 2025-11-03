#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "crossword"
DESCRIPTION = "A crossword game built with Flutter."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "CLOSED"

SRCREV = "22fd549dc0bb6d20ae9424e57431e4c44752722f"
SRC_URI = "git://github.com/flutter/games.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "crossword"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-games-samples-crossword"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "samples/crossword"

inherit flutter-app
