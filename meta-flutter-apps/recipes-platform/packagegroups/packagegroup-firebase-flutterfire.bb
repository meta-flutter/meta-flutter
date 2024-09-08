#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter firebase flutterfire apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    firebase-flutterfire-packages-firebase-in-app-messaging-firebase-in-app-messaging-example \
    firebase-flutterfire-packages-firebase-database-firebase-database-example \
    firebase-flutterfire-packages-firebase-storage-firebase-storage-example \
    firebase-flutterfire-packages-firebase-remote-config-firebase-remote-config-example \
    firebase-flutterfire-packages-firebase-app-installations-firebase-app-installations-example \
    firebase-flutterfire-packages-firebase-core-firebase-core-example \
    firebase-flutterfire-packages-firebase-vertexai-firebase-vertexai-example-vertex-ai-example \
    firebase-flutterfire-packages-firebase-auth-firebase-auth-example \
    firebase-flutterfire-packages-firebase-performance-firebase-performance-example \
    firebase-flutterfire-packages-firebase-app-check-firebase-app-check-example \
    firebase-flutterfire-packages-cloud-firestore-cloud-firestore-example \
    firebase-flutterfire-packages-firebase-messaging-firebase-messaging-example \
    firebase-flutterfire-packages-firebase-ml-model-downloader-firebase-ml-model-downloader-example \
    firebase-flutterfire-packages-firebase-dynamic-links-firebase-dynamic-links-example \
    firebase-flutterfire-packages-cloud-functions-cloud-functions-example \
    firebase-flutterfire-packages-firebase-crashlytics-firebase-crashlytics-example \
    firebase-flutterfire-packages-firebase-analytics-firebase-analytics-example \
"
