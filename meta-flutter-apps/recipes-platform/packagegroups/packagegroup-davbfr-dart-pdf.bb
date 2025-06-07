#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter davbfr dart_pdf apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    davbfr-dart-pdf-demo-printing-demo \
    davbfr-dart-pdf-printing-example \
"
