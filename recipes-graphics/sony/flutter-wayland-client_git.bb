#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

DESCRIPTION = "Flutter Embedder with Wayland Client Backend."
CVE_PRODUCT = "flutter-client"

REQUIRED_DISTRO_FEATURES += "wayland"

require sony-flutter.inc

DEPENDS += "\
    wayland \
    wayland-native \
    "

do_install() {
    install -D -m0755 ${B}/flutter-client \
        ${D}${bindir}/flutter-client
}

FILES_${PN} = "${bindir}"
