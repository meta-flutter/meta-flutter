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
# NOTICE is new at 0.3.3 and carries the Apache-2.0 section 4(d)
# attribution, so it is checksummed with the license text it belongs to.
LIC_FILES_CHKSUM = "\
    file://LICENSE;md5=86e590365d39c174b8169fbff5df50a7 \
    file://NOTICE;md5=552cf7c610f97eaad7ed84a1d847f1ee \
    "

# gitsm: native/third_party/sdbus-cpp is a submodule, add_subdirectory'd by the
# native build hook.
SRCREV = "1f9e6c889385fd3dc8581083088714eee919a83f"
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
