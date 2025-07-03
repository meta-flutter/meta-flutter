#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "shared_preferences_tool"
DESCRIPTION = "DevTools extension for package:shared_preferences. Manage SharedPreferences efficiently. Edit, search, and view keys."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "fe5b25ea2a2e21ab37bf91c9d4a0c83665bcf988"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "shared_preferences_tool"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-shared-preferences-shared-preferences-shared-preferences-tool"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/shared_preferences/shared_preferences_tool"

inherit flutter-app
