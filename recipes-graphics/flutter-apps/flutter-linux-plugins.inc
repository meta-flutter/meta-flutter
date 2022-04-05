SUMMARY = "Flutter Plugin Test Application"
DESCRIPTION = "Flutter Plugin Test Application"
AUTHOR = "Joel Winarske"
HOMEPAGE = "https://github.com/meta-flutter/linux_plugins"
BUGTRACKER = "https://github.com/meta-flutter/linux_plugins/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=df6bd2163489eedcdea6b9406bcbe1dd"

SRC_URI = "git://github.com/meta-flutter/linux_plugins.git;lfs=1;branch=main;protocol=https;destsuffix=git"

SRCREV = "24e3087f5aae4008116123fa47602d189a00372e"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "linux_plugins"
FLUTTER_APPLICATION_INSTALL_PREFIX = "flutter/examples"

inherit flutter-app
