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

SRCREV = "cfcb9dbda56697fe8bafe4b64c1a9261dde908ae"
SRC_URI = "git://github.com/flutter/gallery.git;lfs=0;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "gallery"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-gallery"
FLUTTER_APPLICATION_PATH = ""

inherit flutter-app

RDEPENDS:${PN} += " \
    xdg-user-dirs \
"
