#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "image_picker_example"
DESCRIPTION = "Demonstrates how to use the image_picker plugin."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "b4e0fc1bc117d0248c95204dc1985611adc86b15"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "image_picker_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-image-picker-image-picker-image-picker-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/image_picker/image_picker/example"

inherit flutter-app
