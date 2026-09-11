#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "Package of Flutter flutter samples apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    flutter-samples-material-3-demo \
    flutter-samples-pedometer-example \
    flutter-samples-desktop-photo-search-material \
    flutter-samples-desktop-photo-search-fluent-ui \
    flutter-samples-dynamic-theme \
    flutter-samples-platform-view-swift \
    flutter-samples-platform-channels \
    flutter-samples-date-planner \
    flutter-samples-android-splash-screen-splash-screen-sample \
    flutter-samples-platform-design \
    flutter-samples-cupertino-gallery \
    flutter-samples-simple-sdf \
    flutter-samples-asset-transformation \
    flutter-samples-testing-app \
    flutter-samples-background-isolate-channels \
    flutter-samples-ios-app-clip \
    flutter-samples-navigation-and-routing-bookstore \
    flutter-samples-google-maps-google-maps-in-flutter \
    flutter-samples-compass-app \
    flutter-samples-form-app \
    flutter-samples-animations \
    flutter-samples-simple-shader \
    flutter-samples-add-to-app-multiple-flutters-multiple-flutters-module \
    flutter-samples-add-to-app-books-flutter-module-books \
    flutter-samples-add-to-app-android-view-flutter-module-using-plugin-android-view \
    flutter-samples-add-to-app-android-view-flutter-module-using-plugin-content-sizing-android-view \
    flutter-samples-add-to-app-fullscreen-flutter-module-fullscreen \
    flutter-samples-add-to-app-plugin-flutter-module-using-plugin \
    flutter-samples-add-to-app-prebuilt-module-flutter-module \
    flutter-samples-add-to-app-ios-content-resizing-flutter-module \
    flutter-samples-add-to-app-ios-content-resizing-ios-content-resizing-flutter-module \
"
