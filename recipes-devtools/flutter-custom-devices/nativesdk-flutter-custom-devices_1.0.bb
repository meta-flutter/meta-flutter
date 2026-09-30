#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "Merge an SDK's Flutter custom-device configs into the host config"
DESCRIPTION = "Host-side helper for the custom-device configs that \
conf/include/flutter-custom-device.inc installs into the SDK target sysroot. \
It merges one or all of them into the developer's custom_devices.json, \
replacing entries with a matching id and leaving every other entry alone, and \
fills in the board address and the SDK paths that cannot be known at build \
time. See meta-flutter#522."
AUTHOR = "Joel Winarske"
HOMEPAGE = "https://github.com/meta-flutter/meta-flutter"
SECTION = "devel"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://flutter-custom-devices"

inherit nativesdk

# Deliberately no nativesdk-python3 dependency. This is one stdlib-only script
# and every machine that can use a Yocto SDK already has a host python3 on PATH;
# pulling a second interpreter into every SDK for it is not a trade worth making.
INHIBIT_DEFAULT_DEPS = "1"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${UNPACKDIR}/flutter-custom-devices ${D}${bindir}/flutter-custom-devices
}
