#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "google_sign_in_example"
DESCRIPTION = "Example of Google Sign-In plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "58c02e056b9264258b65be0614f1e33697a7e5b8"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "google_sign_in_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-google-sign-in-google-sign-in-google-sign-in-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/google_sign_in/google_sign_in/example"

inherit flutter-app
