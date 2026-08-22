#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter flutter super_dash apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} += " \
    flutter-super-dash-super-dash \
"
