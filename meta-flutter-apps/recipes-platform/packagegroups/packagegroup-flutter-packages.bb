#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter flutter packages apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    flutter-packages-third-party-packages-flutter-svg-example \
    flutter-packages-vector-graphics-example \
    flutter-packages-pointer-interceptor-pointer-interceptor-pointer-interceptor-example \
    flutter-packages-url-launcher-url-launcher-linux-url-launcher-example \
    flutter-packages-pigeon-platform-tests-alternate-language-test-plugin-alternate-language-test-plugin-example \
    flutter-packages-pigeon-platform-tests-test-plugin-test-plugin-example \
    flutter-packages-camera-example \
    flutter-packages-xdg-directories-xdg-directories-example \
    flutter-packages-image-picker-image-picker-image-picker-example \
    flutter-packages-image-picker-image-picker-linux-example \
    flutter-packages-two-dimensional-scrollables-two-dimensional-examples \
    flutter-packages-quick-actions-quick-actions-quick-actions-example \
    flutter-packages-rfw-example-hello \
    flutter-packages-rfw-example-local \
    flutter-packages-rfw-example-remote \
    flutter-packages-palette-generator-image-colors \
    flutter-packages-google-sign-in-google-sign-in-google-sign-in-example \
    flutter-packages-local-auth-local-auth-darwin-local-auth-darwin-example \
    flutter-packages-local-auth-local-auth-local-auth-example \
    flutter-packages-google-adsense-google-adsense-example \
    flutter-packages-flutter-markdown-flutter-markdown-example \
    flutter-packages-go-router-go-router-examples \
    flutter-packages-go-router-builder-go-router-builder-example \
    flutter-packages-flutter-lints-example \
    flutter-packages-webview-flutter-webview-flutter-webview-flutter-example \
    flutter-packages-webview-flutter-webview-flutter-android-webview-flutter-android-example \
    flutter-packages-google-maps-flutter-google-maps-flutter-google-maps-flutter-example \
    flutter-packages-google-maps-flutter-google-maps-flutter-ios-example-google-maps-flutter-example \
    flutter-packages-animations-example \
    flutter-packages-shared-preferences-shared-preferences-linux-shared-preferences-linux-example \
    flutter-packages-flutter-image-flutter-image-example \
    flutter-packages-path-provider-path-provider-linux-pathproviderexample \
    flutter-packages-extension-google-sign-in-as-googleapis-auth-extension-google-sign-in-example \
    flutter-packages-flutter-adaptive-scaffold-flutter-adaptive-scaffold-example \
    flutter-packages-espresso-example \
    flutter-packages-in-app-purchase-in-app-purchase-in-app-purchase-example \
    flutter-packages-interactive-media-ads-interactive-media-ads-example \
"
