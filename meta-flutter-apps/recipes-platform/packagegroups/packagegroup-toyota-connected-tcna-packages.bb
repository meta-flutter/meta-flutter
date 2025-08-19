#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter toyota-connected tcna-packages apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    toyota-connected-tcna-packages-camera-linux-camera-example \
    toyota-connected-tcna-packages-video-player-video-player-linux-video-player-example \
    toyota-connected-tcna-packages-filament-scene-fluorite-examples-demo \
"
