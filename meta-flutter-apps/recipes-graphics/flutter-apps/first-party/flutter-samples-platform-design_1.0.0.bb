#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "platform_design"
DESCRIPTION = "A project showcasing a Flutter app following different platform IA conventions."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "4b5a101aa26d7d7ddd734acd855eb19d203d9ed5"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "platform_design"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-platform-design"
FLUTTER_APPLICATION_PATH = "platform_design"

inherit flutter-app
