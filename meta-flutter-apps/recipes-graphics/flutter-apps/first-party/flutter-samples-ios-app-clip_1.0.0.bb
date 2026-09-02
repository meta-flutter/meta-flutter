#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "ios_app_clip"
DESCRIPTION = "An example Flutter project that can build as an App Clip."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0 AND BSD-3-Clause AND OFL-1.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "463e365e4842f252ffab9c6198594a504d69469f"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "ios_app_clip"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-ios-app-clip"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "ios_app_clip"

inherit flutter-app
