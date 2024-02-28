#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter baseflow flutter-geolocator apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    baseflow-flutter-geolocator-linux-geolocator-linux-example \
"
