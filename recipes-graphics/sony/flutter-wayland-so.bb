#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

DESCRIPTION = "Flutter Embedder with Wayland Client Backend as shared object (.so)."
CVE_PRODUCT = "flutter_elinux_wayland.so"

REQUIRED_DISTRO_FEATURES += "wayland"

require sony-flutter.inc

DEPENDS += "\
    wayland \
    wayland-native \
    "

EXTRA_OECMAKE += "-DBUILD_ELINUX_SO=ON"

do_install() {
    install -d ${D}${libdir}
    install -m 0755 ${B}/libflutter_elinux_wayland.so ${D}${libdir}
}

FILES:${PN} = "${libdir}"
