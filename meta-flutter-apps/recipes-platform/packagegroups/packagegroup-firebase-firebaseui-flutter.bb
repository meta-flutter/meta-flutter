#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter firebase firebaseui-flutter apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    firebase-firebaseui-flutter-tests \
    firebase-firebaseui-flutter-packages-firebase-ui-localizations-example \
    firebase-firebaseui-flutter-packages-firebase-ui-auth-example \
    firebase-firebaseui-flutter-packages-firebase-ui-storage-example \
    firebase-firebaseui-flutter-packages-firebase-ui-database-example \
    firebase-firebaseui-flutter-packages-firebase-ui-oauth-example \
    firebase-firebaseui-flutter-packages-firebase-ui-firestore-example \
"
