#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "flutter_module_books"
DESCRIPTION = "A Flutter module using the Pigeon package to demonstrate integrating Flutter in a realistic scenario where the existing platform app already has business logic and middleware constraints."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0 & BSD-3-Clause & OFL-1.1"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "8a4cf1db16d52741f0e59e1bfe818723430c35bc"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "flutter_module_books"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-add-to-app-books-flutter-module-books"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "add_to_app/books/flutter_module_books"

inherit flutter-app
