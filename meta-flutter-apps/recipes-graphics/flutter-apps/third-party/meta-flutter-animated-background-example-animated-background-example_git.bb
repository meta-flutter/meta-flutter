#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "animated_background_example"
DESCRIPTION = "A new Flutter project."
AUTHOR = "Andre Baltazar"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1342931ae82e9996a07db6b1fed984fc"

SRCREV = "2482e1e7feae151f03680deabb2a5f6719af87f9"
SRC_URI = "git://github.com/meta-flutter/flutter_animated_background.git;lfs=0;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "animated_background_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "meta-flutter-animated-background-example-animated-background-example"
FLUTTER_APPLICATION_PATH = "example"

inherit flutter-app
