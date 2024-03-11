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

SRCREV = "e18601231e03fbb2ec7e89ce3e7d09c02dff22c9"
SRC_URI = "git://github.com/flutter/games.git;lfs=1;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "ads"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-games-samples-ads"
FLUTTER_APPLICATION_PATH = "samples/ads"

inherit flutter-app
