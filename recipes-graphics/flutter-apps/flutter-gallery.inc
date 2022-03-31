SUMMARY = "Flutter Gallery Application"
DESCRIPTION = "Flutter Gallery Application"
AUTHOR = "Google"
HOMEPAGE = "https://github.com/flutter/gallery"
BUGTRACKER = "https://github.com/flutter/gallery/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3ac21e3d8ebe7dd79f273ca11b9e7b4e"

DEPENDS += " \
    adwaita-icon-theme \
    flutter-engine-${FLUTTER_RUNTIME} \
    "

SRC_URI = "git://github.com/flutter/gallery.git;lfs=0;protocol=https;destsuffix=git"

SRCREV = "9eb785cb997ff56c46e933c1c591f0a6f31454f6"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "gallery"
FLUTTER_APPLICATION_RUN_TEST = "1"
FLUTTER_APPLICATION_CLEAR_LINUX_FOLDER = "1"
FLUTTER_APPLICATION_INSTALL_PREFIX = "flutter/examples"

inherit flutter-app

FILES_${PN} += "${datadir}/homescreen"
