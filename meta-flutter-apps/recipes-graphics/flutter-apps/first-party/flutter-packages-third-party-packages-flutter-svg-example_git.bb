#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "flutter_svg_example"
DESCRIPTION = "An SVG samnple app."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=2b36ca50262dc615e560c27654badb26"

SRCREV = "799b62cc6dd5f71951abd06743addca0ee725f96"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "flutter_svg_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-third-party-packages-flutter-svg-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "third_party/packages/flutter_svg/example"

inherit flutter-app
