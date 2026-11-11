#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "flutter_module_content_resizing"
DESCRIPTION = "An example Flutter module that resizes itself to fit its content."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0 AND BSD-3-Clause AND OFL-1.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "12a191622909214b516b0626fc1413be1000d12c"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "flutter_module_content_resizing"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-add-to-app-ios-content-resizing-flutter-module-flutter-module-content-resizing"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "add_to_app/ios_content_resizing/flutter_module"

inherit flutter-app
