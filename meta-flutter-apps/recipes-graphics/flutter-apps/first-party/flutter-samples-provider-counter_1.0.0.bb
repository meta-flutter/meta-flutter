#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "provider_counter"
DESCRIPTION = "The starter Flutter application, but using Provider to manage state."
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=b7eeb61b41ae366e94383bca5e113fce"

SRCREV = "2999d738b8c088a1438f9446331a36fc7094ba65"
SRC_URI = "git://github.com/flutter/samples.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "provider_counter"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-samples-provider-counter"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "provider_counter"

inherit flutter-app

do_compile[network] = "1"

