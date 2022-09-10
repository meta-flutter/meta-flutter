SUMMARY = "Localization Test Application"
DESCRIPTION = "Localization Test Application"
AUTHOR = "Lakshydeep Vikram"
HOMEPAGE = "https://github.com/meta-flutter/tests"
BUGTRACKER = "https://github.com/meta-flutter/tests/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=df6bd2163489eedcdea6b9406bcbe1dd"

SRCREV = "c18845d2404394180a30c4583e1e865cdd00d433"
SRC_URI = "git://github.com/meta-flutter/tests.git;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "localization"
FLUTTER_APPLICATION_PATH = "localization"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

FLUTTER_BUILD_ARGS = "bundle"

inherit flutter-app
