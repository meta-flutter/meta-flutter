#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "Package of Flutter googleads googleads-mobile-flutter apps"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS_${PN} += " \
    googleads-flutter-samples-admob-mediation-example-mediationexample \
    googleads-flutter-samples-admob-api-demo \
    googleads-flutter-packages-mediation-gma-mediation-line-example \
    googleads-flutter-packages-mediation-gma-mediation-bidmachine-example \
    googleads-flutter-packages-mediation-gma-mediation-liftoffmonetize-example \
    googleads-flutter-packages-mediation-gma-mediation-mintegral-example \
    googleads-flutter-packages-mediation-gma-mediation-moloco-example \
    googleads-flutter-packages-mediation-gma-mediation-applovin-example \
    googleads-flutter-packages-mediation-gma-mediation-maio-example \
    googleads-flutter-packages-mediation-gma-mediation-meta-example \
    googleads-flutter-packages-mediation-gma-mediation-inmobi-example \
    googleads-flutter-packages-mediation-gma-mediation-mytarget-example \
    googleads-flutter-packages-mediation-gma-mediation-dtexchange-example \
    googleads-flutter-packages-mediation-gma-mediation-pangle-example \
    googleads-flutter-packages-mediation-gma-mediation-unity-example \
    googleads-flutter-packages-mediation-gma-mediation-chartboost-example \
    googleads-flutter-packages-mediation-gma-mediation-imobile-example \
    googleads-flutter-packages-mediation-gma-mediation-ironsource-example \
    googleads-flutter-packages-mediation-gma-mediation-pubmatic-example \
    googleads-flutter-packages-google-mobile-ads-example \
"
