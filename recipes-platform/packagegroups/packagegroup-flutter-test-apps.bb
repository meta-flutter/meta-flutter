#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter Test Apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    baseflow-geolocator \
    \
    bluefire-audioplayers-example \
    flutter-animated-background \
    \
    flutter-packages-example-extension-google-sign-in \
    flutter-packages-example-file-selector \
    flutter-packages-example-go-router \
    flutter-packages-example-google-sign-in \
    flutter-packages-example-markdown \
    flutter-packages-example-path-provider \
    flutter-packages-example-shared-preferences \
    flutter-packages-example-url-launcher \
    \
    flutter-games-sample-multiplayer \
    flutter-games-template-basic \
    flutter-games-template-card \
    flutter-games-template-endless-runner \
    \
    flutter-gallery \
    \
    flutterfire-cloud-firestore \
    flutterfire-firebase-core \
    \
    knopp-layer-playground \
    sourcya-playx-3d-scene \
    steenbakker-secure-storage-example \
    toyota-test-texture-egl \
    vgv-super-dash \
"
