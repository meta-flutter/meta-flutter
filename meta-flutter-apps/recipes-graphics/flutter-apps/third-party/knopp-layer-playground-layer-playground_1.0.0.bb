#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "layer_playground"
DESCRIPTION = "A new Flutter project."
AUTHOR = "Matej Knopp"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "CLOSED"

SRCREV = "690ccb793dade39c8082c2606b407a949d56cb8c"
SRC_URI = "git://github.com/knopp/layer_playground.git;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "layer_playground"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "knopp-layer-playground-layer-playground"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = ""

inherit flutter-app
