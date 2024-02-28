#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter google generative-ai-dart apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    google-generative-ai-dart-samples-flutter-app-generative-ai-flutter \
"
