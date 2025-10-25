#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "extension_google_sign_in_example"
DESCRIPTION = "Example of Google Sign-In plugin and googleapis."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "169d55502eca9ca5d72da960cd23e20bbcb8a20e"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "extension_google_sign_in_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-extension-google-sign-in-as-googleapis-auth-extension-google-sign-in-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/extension_google_sign_in_as_googleapis_auth/example"

inherit flutter-app
