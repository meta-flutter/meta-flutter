#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter googleads googleads-mobile-flutter apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    googleads-flutter-samples-admob-native-platform-example \
    googleads-flutter-samples-admob-rewarded-interstitial-example \
    googleads-flutter-samples-admob-mediation-example-mediationexample \
    googleads-flutter-samples-admob-interstitial-example \
    googleads-flutter-samples-admob-banner-example \
    googleads-flutter-samples-admob-native-template-example \
    googleads-flutter-samples-admob-app-open-example \
    googleads-flutter-samples-admob-rewarded-example \
    googleads-flutter-packages-google-mobile-ads-example \
    googleads-flutter-packages-mediation-gma-mediation-liftoffmonetize-example \
    googleads-flutter-packages-mediation-gma-mediation-ironsource-example \
    googleads-flutter-packages-mediation-gma-mediation-meta-example \
    googleads-flutter-packages-mediation-gma-mediation-inmobi-example \
    googleads-flutter-packages-mediation-gma-mediation-pangle-example \
    googleads-flutter-packages-mediation-gma-mediation-dtexchange-example \
    googleads-flutter-packages-mediation-gma-mediation-applovin-example \
    googleads-flutter-packages-mediation-gma-mediation-mintegral-example \
    googleads-flutter-packages-mediation-gma-mediation-unity-example \
"
