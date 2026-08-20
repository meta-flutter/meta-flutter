#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "testing_app"
DESCRIPTION = "A sample that shows testing in Flutter."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "0c5ca75d2985ddeca92417bb1235f361d8643e7b"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "testing_app"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-testing-app"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "testing_app"

inherit flutter-app
