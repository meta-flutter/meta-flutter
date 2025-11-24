#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "flutter_module_using_plugin_android_view"
DESCRIPTION = "An example Flutter module that uses a plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "460d6f9b08628c798ed9e9dded01f065b5d5534c"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "flutter_module_using_plugin_android_view"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-add-to-app-android-view-flutter-module-using-plugin-android-view"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "add_to_app/android_view/flutter_module_using_plugin_android_view"

inherit flutter-app
