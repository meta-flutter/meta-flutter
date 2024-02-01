#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Flutter Wonderous Application"
DESCRIPTION = "A showcase app for the Flutter SDK." \
    "Wonderous will educate and entertain as you uncover" \
    "information about some of the most famous structures" \
    "in the world."
AUTHOR = "gskinner"
HOMEPAGE = "https://github.com/gskinnerTeam/flutter-wonderous-app"
BUGTRACKER = "https://github.com/gskinnerTeam/flutter-wonderous-app/issues"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=6570d633a333be7d4362fdecbf311bfd"

SRCREV = "5b37a65f14ca509046fc97833dd2959ea8bfdd46"
SRC_URI = "git://github.com/gskinnerTeam/flutter-wonderous-app.git;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "wonders"

inherit flutter-app
