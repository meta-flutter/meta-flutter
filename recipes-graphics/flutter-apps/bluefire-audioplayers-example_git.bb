#
# Copyright (c) 2023 Joel Winarske. All rights reserved.
#

SUMMARY = "AudioPlayers Functional Test Case"
DESCRIPTION = "A Flutter plugin to play multiple audio files simultaneously"
AUTHOR = "Blue Fire"
HOMEPAGE = "https://github.com/bluefireteam/audioplayers"
BUGTRACKER = "https://github.com/bluefireteam/audioplayers/issues"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=bacf6c2e93531d3d438d05353cede782"

RDEPENDS:${PN} += "\
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
"

SRCREV = "463b2a1149105a25f81d708533d13cc2dd277d6b"
SRC_URI = "git://github.com/bluefireteam/audioplayers.git;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

FLUTTER_PREBUILD_CMD="\
    flutter pub global activate melos; \
    export PATH=$PATH:${WORKDIR}/pub_cache/bin; \
    melos bootstrap \
"

PUBSPEC_APPNAME = "audioplayers_example"
FLUTTER_APPLICATION_PATH = "packages/audioplayers/example"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

inherit flutter-app

do_compile[network] = "1"
