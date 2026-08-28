#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "Package of Flutter mogol flutter_secure_storage apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} += " \
    mogol-flutter-secure-storage-flutter-secure-storage-example \
"
