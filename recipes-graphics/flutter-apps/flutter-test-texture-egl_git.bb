SUMMARY = "ivi-homescreen EGL Texture Test Application"
DESCRIPTION = "ivi-homescreen EGL Texture Test Application"
AUTHOR = "Joel Winarske"
HOMEPAGE = "https://github.com/meta-flutter/tests"
BUGTRACKER = "https://github.com/meta-flutter/tests/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=df6bd2163489eedcdea6b9406bcbe1dd"

SRCREV = "da096d27ee8e5e74c18eb97f91ac03021363ef5b"
SRC_URI = "git://github.com/meta-flutter/tests.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "test_egl"
FLUTTER_APPLICATION_PATH = "textures/test_egl"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

inherit flutter-app

BBCLASSEXTEND = "runtimerelease runtimeprofile runtimedebug"
