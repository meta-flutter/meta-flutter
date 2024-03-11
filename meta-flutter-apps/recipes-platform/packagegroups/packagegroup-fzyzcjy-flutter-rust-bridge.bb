#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter fzyzcjy flutter_rust_bridge apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    fzyzcjy-flutter-rust-bridge-frb-example-gallery \
"
