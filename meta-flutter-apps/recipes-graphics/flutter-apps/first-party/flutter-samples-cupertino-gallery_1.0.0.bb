#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "cupertino_gallery"
DESCRIPTION = "A Flutter project showcasing supported Cupertino components."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0 & BSD-3-Clause & OFL-1.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "a5bccd247163b1017d2eb9320cf3b94a95ccfb5e"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "cupertino_gallery"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-cupertino-gallery"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "cupertino_gallery"

inherit flutter-app
