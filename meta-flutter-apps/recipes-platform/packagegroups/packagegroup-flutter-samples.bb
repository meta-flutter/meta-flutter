#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter flutter samples apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    flutter-samples-android-splash-screen-splash-screen-sample \
    flutter-samples-simplistic-calculator \
    flutter-samples-compass-app \
    flutter-samples-veggieseasons \
    flutter-samples-background-isolate-channels \
    flutter-samples-infinite-list-infinitelist \
    flutter-samples-pedometer-example \
    flutter-samples-ios-app-clip \
    flutter-samples-deeplink-store-example \
    flutter-samples-context-menus \
    flutter-samples-provider-shopper \
    flutter-samples-flutter-maps-firestore \
    flutter-samples-code-sharing-client \
    flutter-samples-animations \
    flutter-samples-game-template \
    flutter-samples-form-app \
    flutter-samples-testing-app \
    flutter-samples-dynamic-theme \
    flutter-samples-platform-view-swift \
    flutter-samples-experimental-federated-plugin-federated-plugin-example \
    flutter-samples-experimental-varfont-shader-puzzle \
    flutter-samples-material-3-demo \
    flutter-samples-google-maps-google-maps-in-flutter \
    flutter-samples-simplistic-editor \
    flutter-samples-navigation-and-routing-bookstore \
    flutter-samples-isolate-example \
    flutter-samples-provider-counter \
    flutter-samples-platform-channels \
    flutter-samples-platform-design \
    flutter-samples-desktop-photo-search-fluent-ui \
    flutter-samples-desktop-photo-search-material \
    flutter-samples-gemini-tasks \
    flutter-samples-asset-transformation \
    flutter-samples-add-to-app-books-flutter-module-books \
    flutter-samples-add-to-app-fullscreen-flutter-module \
    flutter-samples-add-to-app-plugin-flutter-module-using-plugin \
    flutter-samples-add-to-app-android-view-flutter-module-using-plugin \
    flutter-samples-add-to-app-multiple-flutters-multiple-flutters-module \
    flutter-samples-add-to-app-prebuilt-module-flutter-module \
    flutter-samples-simple-shader \
    flutter-samples-place-tracker \
    flutter-samples-date-planner \
"
