#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "native_template_example"
DESCRIPTION = "Example project for demoing native ads using native templates."
AUTHOR = "Goolge Ads"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "45e52f4674abe9c8fb0b40cec3be8d29163cc090"
SRC_URI = "git://github.com/googleads/googleads-mobile-flutter.git;lfs=0;nobranch=1;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "native_template_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "googleads-flutter-samples-admob-native-template-example"
FLUTTER_APPLICATION_PATH = "samples/admob/native_template_example"

inherit flutter-app
