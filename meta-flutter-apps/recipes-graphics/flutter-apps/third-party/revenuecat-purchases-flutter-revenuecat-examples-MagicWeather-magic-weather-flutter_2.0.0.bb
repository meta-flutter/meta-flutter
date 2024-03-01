#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "magic_weather_flutter"
DESCRIPTION = "Magic Weather sample app for RevenueCat."
AUTHOR = "RevenueCat"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c582549082bd8c4f8e574bd29c5212ed"

SRCREV = "7c8b93639406c73ef68c15f48b5d33d4c0da05ad"
SRC_URI = " \
    git://github.com/RevenueCat/purchases-flutter.git;lfs=0;nobranch=1;protocol=https;destsuffix=git \
    file://purchases-flutter/0001-generic-Linux.patch \ 
"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "magic_weather_flutter"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "revenuecat-purchases-flutter-revenuecat-examples-MagicWeather-magic-weather-flutter"
FLUTTER_APPLICATION_PATH = "revenuecat_examples/MagicWeather"

inherit flutter-app
