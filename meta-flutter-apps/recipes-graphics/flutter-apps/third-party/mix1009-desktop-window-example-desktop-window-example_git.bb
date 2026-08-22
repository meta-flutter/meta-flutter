#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "desktop_window_example"
DESCRIPTION = "Demonstrates how to use the desktop_window plugin."
AUTHOR = "ChunKoo Park"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=317abe6e25431a1de96b8420a663f94b"

SRCREV = "125cab7c41dbf7d3a41791d0d322771897517a65"
SRC_URI = "git://github.com/mix1009/desktop_window.git;lfs=0;branch=master;protocol=https"

PUBSPEC_APPNAME = "desktop_window_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "mix1009-desktop-window-example-desktop-window-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "example"

inherit flutter-app
