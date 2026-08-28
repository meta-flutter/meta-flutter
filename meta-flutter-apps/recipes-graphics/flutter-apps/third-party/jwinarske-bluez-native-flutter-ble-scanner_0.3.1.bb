#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
SUMMARY = "flutter_ble_scanner"
DESCRIPTION = "Flutter Linux desktop app demonstrating the bluez_native package."
AUTHOR = "Joel Winarske"
HOMEPAGE = "https://github.com/jwinarske/bluez_native"
BUGTRACKER = "https://github.com/jwinarske/bluez_native/issues"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=86d3f3a95c324c9479bd8986968f4327"

# gitsm: native/third_party/sdbus-cpp is a submodule, add_subdirectory'd by the
# native build hook.
SRCREV = "77d4c4c3060cf8a0fbce738381cd532f39be68d9"
SRC_URI = "gitsm://github.com/jwinarske/bluez_native.git;branch=main;protocol=https"

# The vendored sdbus-c++ needs an sd-bus implementation at configure time.
# systemd only exists as a build dependency when the distro has the feature;
# fall back to basu (meta-oe) otherwise.
SDBUS_PROVIDER = "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', 'basu', d)}"
DEPENDS += "${SDBUS_PROVIDER}"

FLUTTER_APPLICATION_PATH = "example/flutter_ble_scanner"
PUBSPEC_APPNAME = "flutter_ble_scanner"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "bluez-native-example-flutter-ble-scanner"

inherit flutter-app-native

# bluez_nc talks to BlueZ over D-Bus; the daemon must be on the image.
RDEPENDS:${PN} += "bluez5"
