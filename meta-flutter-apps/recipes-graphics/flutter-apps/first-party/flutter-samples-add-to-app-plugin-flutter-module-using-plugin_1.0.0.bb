#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "flutter_module_using_plugin"
DESCRIPTION = "An example Flutter module that uses a plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "744a2653c915203b6b4d6bac48842fb87fcb9bb6"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "flutter_module_using_plugin"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-add-to-app-plugin-flutter-module-using-plugin"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "add_to_app/plugin/flutter_module_using_plugin"

inherit flutter-app
