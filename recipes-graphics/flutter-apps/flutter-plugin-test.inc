SUMMARY = "Flutter Plugin Test Application"
DESCRIPTION = "Flutter Plugin Test Application"
AUTHOR = "Joel Winarske"
HOMEPAGE = "https://github.com/meta-flutter/linux_plugin_test"
BUGTRACKER = "https://github.com/meta-flutter/linux_plugin_test/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3ac21e3d8ebe7dd79f273ca11b9e7b4e"

SRC_URI = "git://github.com/flutter/gallery.git;lfs=0;protocol=https;destsuffix=git"

SRCREV = "3d355e7ca1407b4b8bbc113796e824a80be2438e"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "linux_plugin_test"
FLUTTER_APPLICATION_INSTALL_PREFIX = "flutter/examples"

inherit flutter-app
