SUMMARY = " Animated Backgrounds for Flutter"
DESCRIPTION = " Animated Backgrounds for Flutter"
AUTHOR = "André Baltazar"
HOMEPAGE = "https://github.com/AndreBaltazar8/flutter_animated_background"
BUGTRACKER = "https://github.com/AndreBaltazar8/flutter_animated_background/issues"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1342931ae82e9996a07db6b1fed984fc"

SRCREV = "46f35ad2a323ab006813a3c4748163536b0fc332"
SRC_URI = "git://github.com/meta-flutter/flutter_animated_background.git;lfs=0;branch=meta-flutter;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "animated_background_example"
FLUTTER_APPLICATION_PATH = "example"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

FLUTTER_BUILD_ARGS = "bundle"

inherit flutter-app
