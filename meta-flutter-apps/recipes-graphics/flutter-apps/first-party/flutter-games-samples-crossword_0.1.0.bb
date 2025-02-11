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

SRCREV = "f51657c94185e72d550f28a9fbc5e5c0d7e94c44"
SRC_URI = "git://github.com/flutter/games.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "crossword"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-games-samples-crossword"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "samples/crossword"

inherit flutter-app
