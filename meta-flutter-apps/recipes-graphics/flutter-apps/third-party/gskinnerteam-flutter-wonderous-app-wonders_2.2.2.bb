#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "wonders"
DESCRIPTION = "Explore the famous wonders of the world."
AUTHOR = "gskinner team"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=6570d633a333be7d4362fdecbf311bfd"

SRCREV = "a12a4d7ed6f98ee230f512c7651fb4e789fe90c4"
SRC_URI = "git://github.com/gskinnerTeam/flutter-wonderous-app.git;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "wonders"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "gskinnerteam-flutter-wonderous-app-wonders"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = ""

inherit flutter-app

do_compile[network] = "1"

