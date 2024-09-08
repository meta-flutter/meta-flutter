#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter fluttercommunity plus_plugins apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    fluttercommunity-plus-plugins-packages-share-plus-share-plus-example \
    fluttercommunity-plus-plugins-packages-connectivity-plus-connectivity-plus-example \
    fluttercommunity-plus-plugins-packages-battery-plus-battery-plus-example \
    fluttercommunity-plus-plugins-packages-device-info-plus-device-info-plus-example \
    fluttercommunity-plus-plugins-packages-package-info-plus-package-info-plus-example \
    fluttercommunity-plus-plugins-packages-network-info-plus-network-info-plus-example \
    fluttercommunity-plus-plugins-packages-sensors-plus-sensors-plus-example \
"
