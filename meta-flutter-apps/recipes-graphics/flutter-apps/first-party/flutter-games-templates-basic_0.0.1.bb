#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "basic"
DESCRIPTION = "A basic game built in Flutter."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0 & BSD-3-Clause & OFL-1.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "ae636d23deae83fd0e7fec9b862a7fcdf2bcfdd8"
SRC_URI = "git://github.com/flutter/games.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "basic"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-games-templates-basic"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "templates/basic"

inherit flutter-app
