#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "simple_shader"
DESCRIPTION = "Using a shader, simply."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "78990c66885e6fd90ab2fe2098f9eb8d5764f3ef"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "simple_shader"
FLUTTER_APPLICATION_PATH = "simple_shader"

inherit flutter-app
