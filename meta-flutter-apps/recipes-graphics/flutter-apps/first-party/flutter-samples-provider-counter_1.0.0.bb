#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "provider_counter"
DESCRIPTION = "The starter Flutter application, but using Provider to manage state."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "460d6f9b08628c798ed9e9dded01f065b5d5534c"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "provider_counter"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-provider-counter"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "provider_counter"

inherit flutter-app

do_compile[network] = "1"

