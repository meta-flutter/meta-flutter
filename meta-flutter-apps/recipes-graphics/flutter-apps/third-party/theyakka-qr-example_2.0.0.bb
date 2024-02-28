#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "example"
DESCRIPTION = "The QR.Flutter example app."
AUTHOR = "Yakka"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=6c189f90a66a3490dde93cebc211f9c0"

SRCREV = "ffbb276ec6ec2830ca50841a7aab9cfb4b7fb26b"
SRC_URI = "git://github.com/theyakka/qr.flutter.git;lfs=0;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "theyakka-qr-example"
FLUTTER_APPLICATION_PATH = "example"

inherit flutter-app
