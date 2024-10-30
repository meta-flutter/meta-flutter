#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "ads"
DESCRIPTION = "A basic game with a banner ad."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "CLOSED"

SRCREV = "c745ac1c35ddbaa00ae4e45cfe537ccbe9a20d87"
SRC_URI = "git://github.com/flutter/games.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "ads"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-games-samples-ads"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "samples/ads"

inherit flutter-app
