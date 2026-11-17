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

# Two licenses because the package ships two libraries. The app's own
# libbluez_nc.so is Apache-2.0; libsdbus-c++.so.2 sits beside it and is
# LGPL-2.1. 0.3.4 stopped linking sdbus-c++ statically, so its object code is
# no longer inside the Apache-2.0 library -- which is the point of the change,
# since a static link brought LGPL section 6 with it -- but the library is
# still shipped, so its license is still this package's.
#
# The submodule also carries an LGPL exception, which covers header material
# only and preserves section 6, so it does not change the license named here.
# There is no SPDX identifier for it, so the text is checksummed instead of
# written into LICENSE as a WITH expression that AVAILABLE_LICENSES would
# reject. NOTICE is the Apache-2.0 section 4(d) attribution.
LICENSE = "Apache-2.0 & LGPL-2.1-only"
LIC_FILES_CHKSUM = "\
    file://LICENSE;md5=86e590365d39c174b8169fbff5df50a7 \
    file://NOTICE;md5=552cf7c610f97eaad7ed84a1d847f1ee \
    file://native/third_party/sdbus-cpp/COPYING;md5=1803fa9c2c3ce8cb06b4861d75310742 \
    file://native/third_party/sdbus-cpp/COPYING-LGPL-Exception;md5=263c8172d75b98e3e2962dc32bd5c3eb \
    "

# gitsm: native/third_party/sdbus-cpp is a submodule, add_subdirectory'd by the
# native build hook. From 0.3.4 it is built shared rather than static, and the
# hook declares it as a second asset, so libsdbus-c++.so.2 installs alongside
# libbluez_nc.so and is resolved from there by $ORIGIN in the library's
# RUNPATH. Newer branches also get an ld.so.conf.d fragment from
# flutter-app-native; this one does not, and does not need one for the
# sibling link.
SRCREV = "042ea604aff8a863e2fddcb26f95bb6f70d476f5"
SRC_URI = "gitsm://github.com/jwinarske/bluez_native.git;branch=main;protocol=https"

# The vendored sdbus-c++ needs an sd-bus implementation at configure time.
# systemd only exists as a build dependency when the distro has the feature;
# fall back to basu (meta-oe) otherwise.
SDBUS_PROVIDER = "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', 'basu', d)}"
DEPENDS += "${SDBUS_PROVIDER}"

FLUTTER_APPLICATION_PATH = "example/flutter_ble_scanner"
PUBSPEC_APPNAME = "flutter_ble_scanner"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "bluez-native-example-flutter-ble-scanner"

# The git fetcher unpacks to ${WORKDIR}/git on this release, while the default
# S is ${WORKDIR}/${BP}. Newer oe-core lines them up with
# BB_GIT_DEFAULT_DESTSUFFIX, which bitbake does not have here, so S is explicit.
S = "${WORKDIR}/git"

inherit flutter-app-native

# Both bundled libraries are private to this app, and shlib tracking has to be
# told so. Neither is something this package offers to others:
# libbluez_nc.so is an app asset the engine dlopens by bare name, and
# libsdbus-c++.so.2 is resolved beside it by $ORIGIN in RUNPATH, not by a
# package dependency. Without this, a recipe elsewhere needing
# libsdbus-c++.so.2 could pick up a dependency on this app package.
#
# On master this line was also a fix: that branch's flutter-app-native
# installs the native assets twice, inside the Flutter bundle and again on
# the linker search path, so one soname registered as two shlib providers
# and do_package failed with "Multiple shlib providers for
# libsdbus-c++.so.2". This branch's class still copies from
# build/native_assets/linux, a path the flutter tool stopped writing, so
# the second copy does not happen here and the failure does not either --
# see #870. The line is still the accurate statement, and it keeps the
# recipe the same across branches for when that block is fixed.
PRIVATE_LIBS:${PN} = "libbluez_nc.so libsdbus-c++.so.2"

# bluez_nc talks to BlueZ over D-Bus; the daemon must be on the image.
RDEPENDS:${PN} += "bluez5"
