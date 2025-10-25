#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "flatpak_flutter_example"
DESCRIPTION = "An Example Flutter application for the flatpak plugin"
AUTHOR = "Toyota Connected North America"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d73cf6ba84211d8b7fd0d2865b678fe8"

SRCREV = "44de1e21bbe1701a498d8303385f116cb07fd708"
SRC_URI = "git://github.com/toyota-connected/tcna-packages.git;lfs=0;branch=v2.0;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "flatpak_flutter_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "toyota-connected-tcna-packages-flatpak-flutter-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/flatpak/example"

inherit flutter-app
