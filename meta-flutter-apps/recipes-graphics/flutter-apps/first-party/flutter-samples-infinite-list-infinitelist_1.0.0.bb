#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "infinitelist"
DESCRIPTION = "A sample implementation of an infinite list."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "21917e3061f933a4f7c630f16fe7a085ac020739"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "infinitelist"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-infinite-list-infinitelist"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "infinite_list"

inherit flutter-app

do_compile[network] = "1"

