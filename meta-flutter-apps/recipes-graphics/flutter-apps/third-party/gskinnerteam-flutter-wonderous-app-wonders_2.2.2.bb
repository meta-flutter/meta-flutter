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

SRCREV = "0331605e1eebfa8199a8fff10d7ee56e4560845f"
SRC_URI = "git://github.com/gskinnerTeam/flutter-wonderous-app.git;lfs=0;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "wonders"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "gskinnerteam-flutter-wonderous-app-wonders"
FLUTTER_APPLICATION_PATH = ""

inherit flutter-app
