#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
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
    install -D -m0755 ${WORKDIR}/build/flutter-client \
        ${D}${bindir}/flutter-client
}

FILES:${PN} = "${bindir}"
