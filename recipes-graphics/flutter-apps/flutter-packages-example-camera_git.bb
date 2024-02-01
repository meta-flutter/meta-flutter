#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Camera Example"
DESCRIPTION = "Example for a Flutter plugin for iOS, Android" \
    "and Web allowing access to the device cameras."
AUTHOR = "Google"
HOMEPAGE = "https://github.com/flutter/packages/tree/main/packages/camera/camera"
BUGTRACKER = "https://github.com/flutter/flutter/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "9f0e92f2273a459fbb0192653e8db4a76dee2faf"
SRC_URI = "git://github.com/flutter/packages.git;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "camera_example"
FLUTTER_APPLICATION_PATH = "packages/camera/camera/example"

inherit flutter-app
