#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "google_maps_in_flutter"
DESCRIPTION = "A new Flutter project."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "3adcdc929a44ac22343a70c2ad4b28400104c01d"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "google_maps_in_flutter"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-google-maps-google-maps-in-flutter"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "google_maps"

inherit flutter-app
