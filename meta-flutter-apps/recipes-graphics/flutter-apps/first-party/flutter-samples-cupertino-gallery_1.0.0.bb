#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "cupertino_gallery"
DESCRIPTION = "A Flutter project showcasing supported Cupertino components."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "0c5ca75d2985ddeca92417bb1235f361d8643e7b"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "cupertino_gallery"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-cupertino-gallery"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "cupertino_gallery"

# The git fetcher unpacks to ${WORKDIR}/git on this release, while the default
# S is ${WORKDIR}/${BP}. Newer oe-core lines them up with
# BB_GIT_DEFAULT_DESTSUFFIX, which bitbake does not have here, so S is explicit.
S = "${WORKDIR}/git"

inherit flutter-app
