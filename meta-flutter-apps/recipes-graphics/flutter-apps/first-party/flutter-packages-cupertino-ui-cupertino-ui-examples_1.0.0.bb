#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "cupertino_ui_examples"
DESCRIPTION = "API code samples for the cupertino_ui package."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "b33d9334f58b1cb6dfc1d6a46deb1c24f4a6425a"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "cupertino_ui_examples"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-cupertino-ui-cupertino-ui-examples"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/cupertino_ui/example"

# The git fetcher unpacks to ${WORKDIR}/git on this release, while the default
# S is ${WORKDIR}/${BP}. Newer oe-core lines them up with
# BB_GIT_DEFAULT_DESTSUFFIX, which bitbake does not have here, so S is explicit.
S = "${WORKDIR}/git"

inherit flutter-app
