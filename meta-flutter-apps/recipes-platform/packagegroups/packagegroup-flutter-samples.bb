#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter flutter samples apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    flutter-samples-form-app \
    flutter-samples-add-to-app-books-flutter-module-books \
    flutter-samples-add-to-app-android-view-flutter-module-using-plugin \
    flutter-samples-add-to-app-multiple-flutters-multiple-flutters-module \
    flutter-samples-add-to-app-prebuilt-module-flutter-module \
    flutter-samples-add-to-app-fullscreen-flutter-module \
    flutter-samples-add-to-app-plugin-flutter-module-using-plugin \
    flutter-samples-code-sharing-client \
    flutter-samples-ios-app-clip \
    flutter-samples-platform-design \
    flutter-samples-navigation-and-routing-bookstore \
    flutter-samples-flutter-maps-firestore \
    flutter-samples-platform-view-swift \
    flutter-samples-asset-transformation \
    flutter-samples-google-maps-google-maps-in-flutter \
    flutter-samples-simplistic-calculator \
    flutter-samples-deeplink-store-example \
    flutter-samples-ai-recipe-generation \
    flutter-samples-infinite-list-infinitelist \
    flutter-samples-game-template \
    flutter-samples-desktop-photo-search-fluent-ui \
    flutter-samples-desktop-photo-search-material \
    flutter-samples-place-tracker \
    flutter-samples-isolate-example \
    flutter-samples-dynamic-theme \
    flutter-samples-context-menus \
    flutter-samples-platform-channels \
    flutter-samples-gemini-tasks \
    flutter-samples-veggieseasons \
    flutter-samples-simplistic-editor \
    flutter-samples-background-isolate-channels \
    flutter-samples-experimental-pedometer-example \
    flutter-samples-experimental-federated-plugin-federated-plugin-example \
    flutter-samples-experimental-varfont-shader-puzzle \
    flutter-samples-experimental-date-planner \
    flutter-samples-provider-shopper \
    flutter-samples-testing-app \
    flutter-samples-simple-shader \
    flutter-samples-android-splash-screen-splash-screen-sample \
    flutter-samples-provider-counter \
    flutter-samples-animations \
    flutter-samples-material-3-demo \
"
