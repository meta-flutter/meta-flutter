#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "provider_shopper"
DESCRIPTION = "A shopping app sample that uses Provider for state management."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "3adcdc929a44ac22343a70c2ad4b28400104c01d"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "provider_shopper"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-provider-shopper"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "provider_shopper"

inherit flutter-app

do_compile[network] = "1"

