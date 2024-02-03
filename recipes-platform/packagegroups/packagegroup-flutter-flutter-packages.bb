#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter flutter packages apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    flutter-packages-pointer-interceptor-pointer-interceptor-example \
    flutter-packages-url-launcher-url-launcher-linux-example \
    flutter-packages-pigeon-platform-tests-alternate-language-test-plugin-example \
    flutter-packages-pigeon-platform-tests-test-plugin-example \
    flutter-packages-camera-camera-example \
    flutter-packages-xdg-directories-example \
    flutter-packages-image-picker-image-picker-example \
    flutter-packages-image-picker-image-picker-linux-example \
    flutter-packages-two-dimensional-scrollables-example \
    flutter-packages-quick-actions-quick-actions-example \
    flutter-packages-rfw-example-hello \
    flutter-packages-rfw-example-local \
    flutter-packages-rfw-example-remote \
    flutter-packages-palette-generator-example \
    flutter-packages-google-sign-in-google-sign-in-example \
    flutter-packages-local-auth-local-auth-example \
    flutter-packages-video-player-video-player-example \
    flutter-packages-flutter-markdown-example \
    flutter-packages-go-router-example \
    flutter-packages-go-router-builder-example \
    flutter-packages-dynamic-layouts-example \
    flutter-packages-platform-example \
    flutter-packages-flutter-lints-example \
    flutter-packages-webview-flutter-webview-flutter-example \
    flutter-packages-webview-flutter-webview-flutter-android-example \
    flutter-packages-google-maps-flutter-google-maps-flutter-example \
    flutter-packages-animations-example \
    flutter-packages-shared-preferences-shared-preferences-linux-example \
    flutter-packages-flutter-image-example \
    flutter-packages-path-provider-path-provider-linux-example \
    flutter-packages-extension-google-sign-in-as-googleapis-auth-example \
    flutter-packages-flutter-adaptive-scaffold-example \
    flutter-packages-espresso-example \
    flutter-packages-in-app-purchase-in-app-purchase-example \
    flutter-packages-file-selector-file-selector-linux-example \
"
