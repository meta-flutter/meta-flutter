#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "package_info_plus_example"
DESCRIPTION = "Demonstrates how to use the package_info_plus plugin."
AUTHOR = "Flutter Community"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=aaa4daf7a83c6896af2c007b59db56e4"

SRCREV = "8e70d3f33d5f1c005dbb1aef733a8a8578989bac"
SRC_URI = "git://github.com/fluttercommunity/plus_plugins.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUB_CACHE_EXTRA_ARCHIVE_PATH = "${WORKDIR}/pub_cache/bin"
PUB_CACHE_EXTRA_ARCHIVE_CMD = "flutter pub global activate melos; \
    melos bootstrap"

PUBSPEC_APPNAME = "package_info_plus_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "fluttercommunity-plus-plugins-packages-package-info-plus-package-info-plus-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/package_info_plus/package_info_plus/example"

inherit flutter-app
