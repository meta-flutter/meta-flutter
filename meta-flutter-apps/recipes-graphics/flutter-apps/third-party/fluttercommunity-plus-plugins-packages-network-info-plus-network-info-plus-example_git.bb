#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "network_info_plus_example"
DESCRIPTION = "Demonstrates how to use the network_info_plus plugin."
AUTHOR = "Flutter Community"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=aaa4daf7a83c6896af2c007b59db56e4"

SRCREV = "2aa9f7e0c7b471dc581d6fbd334633d16eb2da03"
SRC_URI = "git://github.com/fluttercommunity/plus_plugins.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUB_CACHE_EXTRA_ARCHIVE_PATH = "${WORKDIR}/pub_cache/bin"
PUB_CACHE_EXTRA_ARCHIVE_CMD = "flutter pub global activate melos; \
    melos bootstrap"

PUBSPEC_APPNAME = "network_info_plus_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "fluttercommunity-plus-plugins-packages-network-info-plus-network-info-plus-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/network_info_plus/network_info_plus/example"

inherit flutter-app
