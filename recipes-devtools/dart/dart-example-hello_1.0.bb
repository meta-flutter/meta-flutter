#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "Minimal Dart executable built for the target"
DESCRIPTION = "Proves the dart-app class end to end: an AOT snapshot compiled \
on the build machine for the target's architecture, run by the target's \
dartaotruntime. No pub dependencies, so it exercises the toolchain and nothing \
else. See meta-flutter#428."
AUTHOR = "Joel Winarske"
SECTION = "devtools"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://pubspec.yaml \
    file://bin/main.dart \
    "

S = "${WORKDIR}"

inherit dart-app
