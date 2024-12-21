#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "isolate_example"
DESCRIPTION = "A Flutter sample to demonstrate isolates"
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "21917e3061f933a4f7c630f16fe7a085ac020739"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${UNPACKDIR}/git"

PUBSPEC_APPNAME = "isolate_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-isolate-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "isolate_example"

inherit flutter-app

do_compile[network] = "1"

