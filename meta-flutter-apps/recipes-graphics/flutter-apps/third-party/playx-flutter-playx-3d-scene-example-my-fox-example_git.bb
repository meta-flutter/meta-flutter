#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "my_fox_example"
DESCRIPTION = "Demonstrates how to use the playx 3d Scene plugin to show 3d model."
AUTHOR = "Sourcya.io"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=77f824c37447c525bd4906692858848b"

SRCREV = "46cd40546b4b06aea4fae0889926d24e441ad825"
SRC_URI = " \
    git://github.com/playx-flutter/playx-3d-scene.git;lfs=0;branch=main;protocol=https;destsuffix=git \
    file://sourcya-playx-3d-scene/0001-ivi-homescreen-support.patch \
    file://sourcya-playx-3d-scene/textured_pbr.filamat;destsuffix=git/assets/materials \
"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "my_fox_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "playx-flutter-playx-3d-scene-example-my-fox-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "example"

inherit flutter-app
