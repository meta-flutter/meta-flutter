#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "Package of Flutter davbfr dart_pdf apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} += " \
    davbfr-dart-pdf-printing-example \
    davbfr-dart-pdf-demo-printing-demo \
"
