#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

DESCRIPTION = "Build .so file of the Flutter embedding for eLinux with Wayland."
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
    install -m 0755 ${WORKDIR}/build/libflutter_elinux_wayland.so ${D}${bindir}
}

FILES:${PN} = "${libdir}"
