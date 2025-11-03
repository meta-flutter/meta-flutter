#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "background_isolate_channels"
DESCRIPTION = "A new Flutter project."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "744a2653c915203b6b4d6bac48842fb87fcb9bb6"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "background_isolate_channels"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-background-isolate-channels"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "background_isolate_channels"

inherit flutter-app
