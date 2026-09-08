#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

DESCRIPTION = "Flutter Embedder with Wayland Client Backend as shared object (.so)."
CVE_PRODUCT = "flutter_elinux_wayland.so"

REQUIRED_DISTRO_FEATURES += "wayland"

require flutter-elinux.inc

DEPENDS += "\
    wayland \
    wayland-native \
    "

EXTRA_OECMAKE += "-DBUILD_ELINUX_SO=ON"

do_install() {
    install -d ${D}${libdir}
    install -m 0755 ${B}/libflutter_elinux_wayland.so ${D}${libdir}
}

SOLIBS = ".so"
FILES_SOLIBSDEV = ""
FILES:${PN} = "${libdir}"
