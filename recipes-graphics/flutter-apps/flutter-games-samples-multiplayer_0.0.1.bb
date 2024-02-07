#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "multiplayer"
DESCRIPTION = "A game with basic multiplayer support."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "CLOSED"

SRCREV = "7e524544b57e4a8efdf85828721b90acb7c60800"
SRC_URI = "git://github.com/flutter/games.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "multiplayer"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-games-samples-multiplayer"
FLUTTER_APPLICATION_PATH = "samples/multiplayer"

inherit flutter-app
