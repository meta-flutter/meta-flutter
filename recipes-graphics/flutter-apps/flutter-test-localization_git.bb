SUMMARY = "Localization Test Application"
DESCRIPTION = "Localization Test Application"
AUTHOR = "Lakshydeep Vikram"
HOMEPAGE = "https://github.com/meta-flutter/tests"
BUGTRACKER = "https://github.com/meta-flutter/tests/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=df6bd2163489eedcdea6b9406bcbe1dd"

SRCREV = "ce7bb3916197f1ccb2b64064fa3fcdf1307223f7"
SRC_URI = "git://github.com/meta-flutter/tests.git;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "localization"
FLUTTER_APPLICATION_PATH = "localization"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

inherit flutter-app
