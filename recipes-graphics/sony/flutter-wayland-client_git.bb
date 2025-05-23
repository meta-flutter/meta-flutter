#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

DESCRIPTION = "Flutter Embedder with Wayland Client Backend."

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

FILES:${PN} = "${bindir}"
