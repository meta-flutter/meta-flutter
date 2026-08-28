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

LICENSE = "Apache-2.0 & BSD-3-Clause & OFL-1.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "ae636d23deae83fd0e7fec9b862a7fcdf2bcfdd8"
SRC_URI = "git://github.com/flutter/games.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "multiplayer"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-games-samples-multiplayer"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "samples/multiplayer"

inherit flutter-app
