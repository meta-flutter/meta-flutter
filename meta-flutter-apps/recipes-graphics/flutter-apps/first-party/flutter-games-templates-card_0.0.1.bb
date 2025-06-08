#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "card"
DESCRIPTION = "A game built in Flutter."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "CLOSED"

SRCREV = "fa96d266d638ccf9e48e4b8debe6d244240e961d"
SRC_URI = "git://github.com/flutter/games.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "card"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-games-templates-card"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "templates/card"

inherit flutter-app
