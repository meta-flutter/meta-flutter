#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "client"
DESCRIPTION = "A Flutter app which communicates with a Dart backend using shared business logic."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "291ba126bfc1279d074b91e61ad44919768ebe7f"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "client"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-code-sharing-client"
FLUTTER_APPLICATION_PATH = "code_sharing/client"

inherit flutter-app
