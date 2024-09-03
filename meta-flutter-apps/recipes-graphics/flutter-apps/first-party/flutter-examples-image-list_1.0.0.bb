#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "image_list"
DESCRIPTION = "Simple Flutter project used for benchmarking image loading over network."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1d84cf16c48e571923f837136633a265"

SRCREV = "cf47702e5f89592787522d5f70deff52db8628fb"
SRC_URI = "git://github.com/flutter/flutter.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "image_list"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-examples-image-list"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "examples/image_list"

inherit flutter-app
