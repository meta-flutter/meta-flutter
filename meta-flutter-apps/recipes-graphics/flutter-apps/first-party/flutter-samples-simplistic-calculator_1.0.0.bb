#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "simplistic_calculator"
DESCRIPTION = "A new Flutter project."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "bbc6e1f98ac9156647eb5ba132b23ec191c9c6b9"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "simplistic_calculator"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-simplistic-calculator"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "simplistic_calculator"

inherit flutter-app

do_compile[network] = "1"

