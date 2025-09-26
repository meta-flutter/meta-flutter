#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "fluorite_examples_demo"
DESCRIPTION = "Collection of example scenes showcasing the features of the Fluorite Game Engine."
AUTHOR = "Toyota Connected North America"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d73cf6ba84211d8b7fd0d2865b678fe8"

SRCREV = "9df8b4c4a6ff493c29c1ab554845dbafe0dbd9ef"
SRC_URI = "git://github.com/toyota-connected/tcna-packages.git;branch=v2.0;protocol=https"

PUBSPEC_APPNAME = "fluorite_examples_demo"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "toyota-connected-tcna-packages-filament-scene-fluorite-examples-demo"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/filament_scene/example"

inherit flutter-app
