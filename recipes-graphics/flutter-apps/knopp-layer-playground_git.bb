#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Layer Playground"
DESCRIPTION = "Platform Views Functional Test Case"
AUTHOR = "Matej Knopp"
HOMEPAGE = "https://github.com/knopp/layer_playground"
BUGTRACKER = "https://github.com/knopp/layer_playground/issues"
SECTION = "graphics"

LICENSE = "CLOSED"

SRCREV = "690ccb793dade39c8082c2606b407a949d56cb8c"
SRC_URI = "git://github.com/knopp/layer_playground.git;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "layer_playground"

inherit flutter-app
