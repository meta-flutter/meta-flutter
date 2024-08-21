#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "battery_plus_example"
DESCRIPTION = "Demonstrates how to use the battery_plus plugin."
AUTHOR = "Flutter Community"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=aaa4daf7a83c6896af2c007b59db56e4"

SRCREV = "91f48a6bc7d11c4238c9539ca06e6fa768995580"
SRC_URI = "git://github.com/fluttercommunity/plus_plugins.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUB_CACHE_EXTRA_ARCHIVE_PATH = "${WORKDIR}/pub_cache/bin"
PUB_CACHE_EXTRA_ARCHIVE_CMD = "flutter pub global activate melos; \
    melos bootstrap"

PUBSPEC_APPNAME = "battery_plus_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "fluttercommunity-plus-plugins-packages-battery-plus-battery-plus-example"
FLUTTER_APPLICATION_PATH = "packages/battery_plus/battery_plus/example"

inherit flutter-app
