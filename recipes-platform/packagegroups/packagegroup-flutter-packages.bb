#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter flutter packages apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    flutter-packages-pointer-interceptor-pointer-interceptor \
    flutter-packages-url-launcher-url-launcher-linux \
    flutter-packages-pigeon-platform-tests-alternate-language-test-plugin \
    flutter-packages-pigeon-platform-tests-test-plugin \
    flutter-packages-camera \
    flutter-packages-xdg-directories \
    flutter-packages-image-picker-image-picker \
    flutter-packages-image-picker-image-picker-linux \
    flutter-packages-two-dimensional-scrollables \
    flutter-packages-quick-actions-quick-actions \
    flutter-packages-rfw-example \
    flutter-packages-rfw-example \
    flutter-packages-rfw-example \
    flutter-packages-palette-generator \
    flutter-packages-google-sign-in-google-sign-in \
    flutter-packages-local-auth-local-auth \
    flutter-packages-video-player-video-player \
    flutter-packages-flutter-markdown \
    flutter-packages-go-router \
    flutter-packages-go-router-builder \
    flutter-packages-dynamic-layouts \
    flutter-packages-platform \
    flutter-packages-flutter-lints \
    flutter-packages-webview-flutter-webview-flutter \
    flutter-packages-webview-flutter-webview-flutter-android \
    flutter-packages-google-maps-flutter-google-maps-flutter \
    flutter-packages-animations \
    flutter-packages-shared-preferences-shared-preferences-linux \
    flutter-packages-flutter-image \
    flutter-packages-path-provider-path-provider-linux \
    flutter-packages-extension-google-sign-in-as-googleapis-auth \
    flutter-packages-flutter-adaptive-scaffold \
    flutter-packages-espresso \
    flutter-packages-in-app-purchase-in-app-purchase \
    flutter-packages-file-selector-file-selector-linux \
"
