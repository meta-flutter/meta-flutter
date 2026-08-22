#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter knopp layer_playground apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} += " \
    knopp-layer-playground-layer-playground \
"
