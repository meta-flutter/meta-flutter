#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "basic"
DESCRIPTION = "A basic game built in Flutter."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "CLOSED"

SRCREV = "f41e3dd31011aab41dcb3b050d1fceddc3b750c4"
SRC_URI = "git://github.com/flutter/games.git;lfs=1;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "basic"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-games-templates-basic"
FLUTTER_APPLICATION_PATH = "templates/basic"

inherit flutter-app
