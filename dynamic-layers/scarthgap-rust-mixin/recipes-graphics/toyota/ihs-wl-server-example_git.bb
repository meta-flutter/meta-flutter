#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# The ihs_wayland_server example: a Flutter app that starts the embedded
# Wayland server (ihs_wl_server) and shows a client toplevel in a platform
# view. Run it under ivi-homescreen; with LAUNCH=<client> in its environment
# it launches that client itself and binds it by activation token.
#
# The package's build hook builds libihs_wl_server.so with cargo, and it ships
# in the app's bundle: its version is the one the app's pubspec pins, so apps
# on one image can each carry their own.
#
# scarthgap's own Rust (1.75.0) cannot build smithay, so this recipe is only
# seen with meta-lts-mixins scarthgap/rust. bindgen also needs libclang, which
# scarthgap gets from meta-clang.
#

SUMMARY = "ihs_wl_server example app"
DESCRIPTION = "Flutter app that shows Wayland clients inside ivi-homescreen \
platform views through the ihs_wl_server module."
AUTHOR = "joel.winarske@toyotaconnected.com"
HOMEPAGE = "https://github.com/toyota-connected/ihs_wl_server"
BUGTRACKER = "https://github.com/toyota-connected/ihs_wl_server/issues"
SECTION = "graphics"

# The app and library are Apache-2.0; the crates linked into the library are
# MIT or dual MIT/Apache-2.0.
LICENSE = "Apache-2.0 & MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1bb8e4c9af5dd10ccb6c8bae63fb4a2d"

SRC_URI = "git://github.com/toyota-connected/ihs_wl_server.git;protocol=https;branch=main"
SRCREV = "606abc53dc2498f1e7f4c938c6a8e851880a82ab"

PV = "0.1.0+git"
S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "ihs_wayland_server_example"
FLUTTER_APPLICATION_PATH = "dart/ihs_wayland_server/example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "${PN}"

# The example's pubspec.lock is resolved against whatever Flutter the
# upstream developer had; resolve against this SDK instead.
PUBSPEC_IGNORE_LOCKFILE = "1"

# libihs_wl_server: ivi-homescreen's platform-view ABI (1.16 or newer) through
# ivi-homescreen-shared.pc, which bindgen reads; smithay's system libraries.
DEPENDS += "\
    ivi-homescreen-shared \
    libdrm \
    libxkbcommon \
    virtual/libgbm \
"

FLUTTER_NATIVE_CARGO = "1"

inherit flutter-app-native
