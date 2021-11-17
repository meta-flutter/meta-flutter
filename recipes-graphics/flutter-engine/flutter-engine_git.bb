SUMMARY = "Flutter Engine"
DESCRIPTION = "recipe to build Google Flutter Engine for use with Dart applications on embedded Linux"
AUTHOR = "Joel Winarske <joel.winarske@linux.com>"
HOMEPAGE = "https://github.com/jwinarske/meta-flutter/"
BUGTRACKER = "https://github.com/jwinarske/meta-flutter/issues"
SECTION = "graphics"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://flutter/LICENSE;md5=a60894397335535eb10b54e2fff9f265"
CVE_PRODUCT = "libflutter_engine.so"

SRC_URI = "file://0001-clang-toolchain.patch \
           file://0002-x64-sysroot-assert.patch"

FLUTTER_CHANNEL ??= "beta"
FLUTTER_CLANG_VERSION ??= "12.0.0"
TARGET_GCC_VERSION ??= "9.3.0"

DEPOT_TOOLS ??= "${STAGING_DIR_NATIVE}/usr/share/depot_tools"
PYTHON2_PATH ??= "bootstrap-2@3.8.9.chromium.14_bin/python/bin"

inherit flutter-engine
