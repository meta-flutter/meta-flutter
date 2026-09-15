#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "splash_screen_sample"
DESCRIPTION = "A sample Flutter app with animated splash screen on Android 12."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0 & BSD-3-Clause & OFL-1.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "8a4cf1db16d52741f0e59e1bfe818723430c35bc"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "splash_screen_sample"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-android-splash-screen-splash-screen-sample"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "android_splash_screen"

inherit flutter-app
