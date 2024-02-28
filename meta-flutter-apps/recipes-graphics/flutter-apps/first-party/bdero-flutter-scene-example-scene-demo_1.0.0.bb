#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "scene_demo"
DESCRIPTION = "A new Flutter project."
AUTHOR = "Brandon DeRosier"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "CLOSED"

SRCREV = "bc7362b4052c98646d43924b06a1974aa9a2b369"
SRC_URI = "git://github.com/bdero/flutter-scene-example.git;lfs=0;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "scene_demo"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "bdero-flutter-scene-example-scene-demo"
FLUTTER_APPLICATION_PATH = ""

inherit flutter-app
