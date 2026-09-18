#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "Package of Flutter flutter samples apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    flutter-samples-add-to-app-android-view-flutter-module-using-plugin-android-view \
    flutter-samples-add-to-app-android-view-flutter-module-using-plugin-content-sizing-android-view \
    flutter-samples-add-to-app-books-flutter-module-books \
    flutter-samples-add-to-app-fullscreen-flutter-module-fullscreen \
    flutter-samples-add-to-app-ios-content-resizing-flutter-module \
    flutter-samples-add-to-app-ios-content-resizing-ios-content-resizing-flutter-module \
    flutter-samples-add-to-app-multiple-flutters-multiple-flutters-module \
    flutter-samples-add-to-app-plugin-flutter-module-using-plugin \
    flutter-samples-add-to-app-prebuilt-module-flutter-module \
    flutter-samples-android-splash-screen-splash-screen-sample \
    flutter-samples-animations \
    flutter-samples-asset-transformation \
    flutter-samples-background-isolate-channels \
    flutter-samples-compass-app \
    flutter-samples-cupertino-gallery \
    flutter-samples-date-planner \
    flutter-samples-desktop-photo-search-fluent-ui \
    flutter-samples-desktop-photo-search-material \
    flutter-samples-dynamic-theme \
    flutter-samples-form-app \
    flutter-samples-google-maps-google-maps-in-flutter \
    flutter-samples-ios-app-clip \
    flutter-samples-material-3-demo \
    flutter-samples-navigation-and-routing-bookstore \
    flutter-samples-pedometer-example \
    flutter-samples-platform-channels \
    flutter-samples-platform-design \
    flutter-samples-platform-view-swift \
    flutter-samples-simple-sdf \
    flutter-samples-simple-shader \
    flutter-samples-testing-app \
"
