#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "material_3_demo"
DESCRIPTION = "A Flutter project showcasing supported Material 3 components, typography, color system and elevation. Supports different light/dark mode, color seed, and comparison to Material 2."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "78990c66885e6fd90ab2fe2098f9eb8d5764f3ef"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "material_3_demo"
FLUTTER_APPLICATION_PATH = "material_3_demo"

inherit flutter-app
