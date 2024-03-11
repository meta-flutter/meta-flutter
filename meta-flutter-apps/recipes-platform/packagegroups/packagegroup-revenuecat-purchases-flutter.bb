#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter revenuecat purchases-flutter apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    revenuecat-purchases-flutter-revenuecat-examples-purchase-tester-purchases-flutter-example \
    revenuecat-purchases-flutter-revenuecat-examples-magicweather-magic-weather-flutter \
"
