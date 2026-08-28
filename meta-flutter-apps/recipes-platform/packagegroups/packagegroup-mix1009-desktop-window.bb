#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "Package of Flutter mix1009 desktop_window apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    mix1009-desktop-window-example-desktop-window-example \
"
