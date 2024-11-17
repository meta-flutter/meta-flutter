#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

DESCRIPTION = "Flutter Embedder with X11 Backend."
CVE_PRODUCT = "flutter-x11-client"

REQUIRED_DISTRO_FEATURES += "x11"

require sony-flutter.inc

do_install() {
    install -D -m0755 ${WORKDIR}/build/flutter-x11-client \
        ${D}${bindir}/flutter-x11-client
}

FILES:${PN} = "${bindir}"
