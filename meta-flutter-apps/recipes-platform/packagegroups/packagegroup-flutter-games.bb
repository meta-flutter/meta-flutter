#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter flutter games apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    flutter-games-templates-basic \
    flutter-games-templates-endless-runner \
    flutter-games-templates-card \
    flutter-games-samples-ads \
    flutter-games-samples-multiplayer \
    flutter-games-samples-crossword \
"
