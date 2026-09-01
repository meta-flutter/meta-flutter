#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "flutter_gallery"
DESCRIPTION = "The Flutter gallery: material and cupertino demos, the SDK's \
largest example app and the first with dependencies that resolve to something."

PUBSPEC_APPNAME = "flutter_gallery"
FLUTTER_APPLICATION_PATH = "dev/integration_tests/flutter_gallery"

# The manifest entry removed in #872 carried this, having worked out that the
# gallery reaches for the user directories at runtime. Kept rather than
# rediscovered.
RDEPENDS:${PN} += "xdg-user-dirs"

require conf/include/flutter-sdk-app.inc
