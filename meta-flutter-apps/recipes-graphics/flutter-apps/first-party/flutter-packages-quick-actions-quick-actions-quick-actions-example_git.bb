#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "quick_actions_example"
DESCRIPTION = "Demonstrates how to use the quick_actions plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "58c02e056b9264258b65be0614f1e33697a7e5b8"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "quick_actions_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-quick-actions-quick-actions-quick-actions-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/quick_actions/quick_actions/example"

inherit flutter-app
