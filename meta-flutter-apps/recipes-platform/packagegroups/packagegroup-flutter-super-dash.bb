#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter flutter super_dash apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    flutter-super-dash-super-dash \
"
