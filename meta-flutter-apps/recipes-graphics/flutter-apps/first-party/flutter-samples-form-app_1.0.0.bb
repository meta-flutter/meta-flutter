#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "form_app"
DESCRIPTION = "A sample demonstrating different types of forms and best practices"
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "29f5e32bf3e435beb5a44c4b263bcc97cd86ad9a"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${UNPACKDIR}/git"

PUBSPEC_APPNAME = "form_app"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-form-app"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "form_app"

inherit flutter-app

do_compile[network] = "1"

