#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "Package of Flutter flutter samples apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} += " \
    flutter-samples-background-isolate-channels \
    flutter-samples-platform-channels \
    flutter-samples-form-app \
    flutter-samples-simple-sdf \
    flutter-samples-android-splash-screen-splash-screen-sample \
    flutter-samples-testing-app \
    flutter-samples-ios-app-clip \
    flutter-samples-add-to-app-books-flutter-module-books \
    flutter-samples-add-to-app-prebuilt-module-flutter-module \
    flutter-samples-add-to-app-android-view-flutter-module-using-plugin-content-sizing-android-view \
    flutter-samples-add-to-app-android-view-flutter-module-using-plugin-android-view \
    flutter-samples-add-to-app-ios-content-resizing-ios-content-resizing-flutter-module \
    flutter-samples-add-to-app-ios-content-resizing-flutter-module \
    flutter-samples-add-to-app-fullscreen-flutter-module-fullscreen \
    flutter-samples-add-to-app-plugin-flutter-module-using-plugin \
    flutter-samples-add-to-app-multiple-flutters-multiple-flutters-module \
    flutter-samples-pedometer-example \
    flutter-samples-date-planner \
    flutter-samples-dynamic-theme \
    flutter-samples-animations \
    flutter-samples-compass-app \
    flutter-samples-simple-shader \
    flutter-samples-navigation-and-routing-bookstore \
    flutter-samples-cupertino-gallery \
    flutter-samples-asset-transformation \
    flutter-samples-material-3-demo \
    flutter-samples-desktop-photo-search-fluent-ui \
    flutter-samples-desktop-photo-search-material \
    flutter-samples-platform-view-swift \
    flutter-samples-google-maps-google-maps-in-flutter \
    flutter-samples-platform-design \
"
