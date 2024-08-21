#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "gallery"
DESCRIPTION = "A resource to help developers evaluate and use Flutter."
AUTHOR = "Google"
HOMEPAGE = "https://github.com/flutter/gallery"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3ac21e3d8ebe7dd79f273ca11b9e7b4e"

SRCREV = "66a69803cc63dfc02878fae1959a2555f26ea25f"
SRC_URI = " \
    git://github.com/flutter/gallery.git;lfs=0;branch=main;protocol=https;destsuffix=git \
    file://0001-flutter-pub-upgrade-major-versions.patch \
"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "gallery"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-gallery"
FLUTTER_APPLICATION_PATH = ""

inherit flutter-app

RDEPENDS:${PN} += " \
    xdg-user-dirs \
"
