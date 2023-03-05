#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter Test Apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    flutter-gallery \
    flutter-test-frb \
    flutter-test-membrane \
    flutter-test-animated-background \
    flutter-test-membrane \
    flutter-test-secure-storage \
    flutter-test-video-player \
    flutter-test-localization \
"
