#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "crossword"
DESCRIPTION = "A crossword game built with Flutter."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0 & BSD-3-Clause & OFL-1.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "ccff53d6f6a9a7de01e8d1140d061345b6b3819e"
SRC_URI = "git://github.com/flutter/games.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "crossword"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-games-samples-crossword"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "samples/crossword"

inherit flutter-app
