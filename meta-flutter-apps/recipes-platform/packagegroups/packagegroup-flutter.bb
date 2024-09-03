#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter flutter flutter apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    flutter-examples-image-list \
    flutter-examples-layers-flutter-examples-layers \
    flutter-examples-flutter-view \
    flutter-examples-platform-channel-swift \
    flutter-examples-platform-channel \
    flutter-examples-splash \
    flutter-examples-platform-view \
    flutter-examples-texture \
    flutter-examples-hello-world \
    flutter-packages-integration-test-example \
    flutter-dev-manual-tests \
    flutter-dev-a11y-assessments \
    flutter-dev-integration-tests-windows-startup-test \
    flutter-dev-integration-tests-spell-check \
    flutter-dev-integration-tests-abstract-method-smoke-test \
    flutter-dev-integration-tests-wide-gamut-test \
    flutter-dev-integration-tests-gradle-deprecated-settings \
    flutter-dev-integration-tests-channels \
    flutter-dev-integration-tests-flavors \
    flutter-dev-integration-tests-ios-app-with-extensions \
    flutter-dev-integration-tests-ios-platform-view-tests \
    flutter-dev-integration-tests-android-views-platform-views \
    flutter-dev-integration-tests-ui-integration-ui \
    flutter-dev-integration-tests-deferred-components-test \
    flutter-dev-integration-tests-new-gallery \
    flutter-dev-integration-tests-flutter-gallery \
    flutter-dev-integration-tests-web-integration \
    flutter-dev-integration-tests-android-embedding-v2-smoke-test \
    flutter-dev-integration-tests-android-semantics-testing \
    flutter-dev-integration-tests-android-verified-input \
    flutter-dev-integration-tests-release-smoke-test \
    flutter-dev-integration-tests-hybrid-android-views-hybrid-platform-views \
    flutter-dev-integration-tests-non-nullable \
    flutter-dev-integration-tests-ios-add2app-life-cycle-flutterapp \
    flutter-dev-integration-tests-platform-interaction \
    flutter-dev-benchmarks-macrobenchmarks \
    flutter-dev-benchmarks-multiple-flutters-module \
    flutter-dev-benchmarks-platform-views-layout-hybrid-composition \
    flutter-dev-benchmarks-complex-layout \
    flutter-dev-benchmarks-platform-views-layout \
    flutter-dev-benchmarks-platform-channels-benchmarks \
    flutter-dev-benchmarks-test-apps-stocks \
"
