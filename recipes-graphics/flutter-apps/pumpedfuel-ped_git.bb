#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Pumped Fuel"
DESCRIPTION = "Pumped Fuel End Device"
AUTHOR = "Pumped Fuel"
HOMEPAGE = "https://github.com/bernardpumped/ped"
BUGTRACKER = "https://github.com/bernardpumped/ped/issues"
SECTION = "graphics"

LICENSE = "GPL-3.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=e49f4652534af377a713df3d9dec60cb"

SRCREV = "bfe3e304d112acbd2ccfee57da3275ae87c5528d"
SRC_URI = "git://github.com/bernardpumped/ped.git;lfs=0;branch=next_main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "pumped_end_device"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

inherit flutter-app
