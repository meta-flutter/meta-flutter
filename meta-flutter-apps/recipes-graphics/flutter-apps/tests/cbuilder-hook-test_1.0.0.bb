#
# SPDX-License-Identifier: MIT
#

SUMMARY = "Flutter app with a native_toolchain_c build hook"
DESCRIPTION = "Proves flutter-app-native's hook toolchain end to end: a build \
hook that compiles C and C++ with native_toolchain_c's CBuilder, cross-built \
for the target. See meta-flutter#801."
AUTHOR = "meta-flutter"
HOMEPAGE = "https://github.com/meta-flutter/meta-flutter"
BUGTRACKER = "https://github.com/meta-flutter/meta-flutter/issues"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# The app sits beside the recipe, as dart-example-hello's does. It exists to
# exercise the class and has no repository of its own to fetch from.
#
# Its hook runs CBuilder three times: a static archive, a shared library
# linked against it, and a C++ shared library. That is every way CBuilder
# drives a toolchain, and none of them goes through cmake, so the compiler can
# only come from the FLUTTER_HOOK_CC / _AR / _LD wrappers the class hands the
# flutter tool. Without them the hook is given no compiler, and a cross build
# stops in do_compile:
#
#   No compiler configured on host 'linux_x64' with target 'linux_arm64'.
SRC_URI = "file://cbuilder_hook_test"

S = "${UNPACKDIR}/cbuilder_hook_test"

PUBSPEC_APPNAME = "cbuilder_hook_test"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "cbuilder-hook-test"

inherit flutter-app-native
