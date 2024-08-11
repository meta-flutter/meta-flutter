#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "flutter_markdown_example"
DESCRIPTION = "Demonstrates how to use the flutter_markdown package."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=a60894397335535eb10b54e2fff9f265"

SRCREV = "4b00343963a1529598e602f0e3954c9fee9ec7d5"
SRC_URI = "git://github.com/flutter/packages.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "flutter_markdown_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-packages-flutter-markdown-flutter-markdown-example"
FLUTTER_APPLICATION_PATH = "packages/flutter_markdown/example"
PUBSPEC_IGNORE_LOCKFILE = "1"

inherit flutter-app
