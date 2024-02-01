#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Super Dash"
DESCRIPTION = "Super Dash"
AUTHOR = "Very Good Ventures"
HOMEPAGE = "https://github.com/flutter/super_dash"
BUGTRACKER = "https://github.com/flutter/super_dash/issues"
SECTION = "graphics"

LICENSE = "CLOSED"

SRCREV = "d5991328a1675ea24ce4033127de6e39e2a6c9b4"
SRC_URI = "git://github.com/flutter/super_dash.git;lfs=0;branch=main;protocol=https;destsuffix=git \
           file://super_dash/0001-ivi-homescreen-support.patch"

S = "${WORKDIR}/git"

FLUTTER_BUILD_ARGS = "bundle --target lib/main_prod.dart --dart-define FLUTTER_APP_FLAVOR=prod"
APP_AOT_EXTRA = "-DFLUTTER_APP_FLAVOR=prod"
APP_AOT_ENTRY_FILE = "main_prod.dart"

PUBSPEC_APPNAME = "super_dash"

inherit flutter-app

do_compile[network] = "1"
