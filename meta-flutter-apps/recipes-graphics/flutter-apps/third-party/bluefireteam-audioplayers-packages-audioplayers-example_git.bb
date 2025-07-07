#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "audioplayers_example"
DESCRIPTION = "Demonstrates how to use the audioplayers plugin."
AUTHOR = "Luan Nico"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c40600261a3b45d01ebc98bcb0a6b2d5"

SRCREV = "f2269aeb270154e5f3571e1e025d340cfbca4686"
SRC_URI = "git://github.com/bluefireteam/audioplayers.git;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "audioplayers_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "bluefireteam-audioplayers-packages-audioplayers-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/audioplayers/example"

inherit flutter-app

RDEPENDS:${PN} += " \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
"
