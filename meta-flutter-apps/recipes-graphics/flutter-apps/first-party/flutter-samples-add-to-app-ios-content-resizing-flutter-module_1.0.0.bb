#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "flutter_module"
DESCRIPTION = "A new Flutter module project."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "0c5ca75d2985ddeca92417bb1235f361d8643e7b"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "flutter_module"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-add-to-app-ios-content-resizing-flutter-module"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "add_to_app/ios_content_resizing/flutter_module"

# The git fetcher unpacks to ${WORKDIR}/git on this release, while the default
# S is ${WORKDIR}/${BP}. Newer oe-core lines them up with
# BB_GIT_DEFAULT_DESTSUFFIX, which bitbake does not have here, so S is explicit.
S = "${WORKDIR}/git"

inherit flutter-app
