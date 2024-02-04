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

SRCREV = "54af754b5b9ad0e8201484c2db564b78ed07a8bc"
SRC_URI = "git://github.com/flutter/games.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "ads"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-games-samples-ads"
FLUTTER_APPLICATION_PATH = "samples/ads"

inherit flutter-app
