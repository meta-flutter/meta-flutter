SUMMARY = "Flutter Gallery Application"
DESCRIPTION = "Flutter Gallery Application"
AUTHOR = "Google"
HOMEPAGE = "https://github.com/flutter/gallery"
BUGTRACKER = "https://github.com/flutter/gallery/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3ac21e3d8ebe7dd79f273ca11b9e7b4e"

SRC_URI = "git://github.com/flutter/gallery.git;lfs=0;protocol=https;destsuffix=git"

SRCREV = "9eb785cb997ff56c46e933c1c591f0a6f31454f6"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "gallery"

inherit flutter-app

do_install_append () {
    install -d ${D}${datadir}/homescreen/bundle
    ln -sf /usr/share/gallery/ ${D}${datadir}/homescreen/bundle/flutter_assets
}

FILES_${PN} += "${datadir}/homescreen"
