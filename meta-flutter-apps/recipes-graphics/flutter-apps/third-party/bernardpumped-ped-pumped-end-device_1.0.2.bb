#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "pumped_end_device"
DESCRIPTION = "Pumped Fuel is a combination of 3 core projects to connect vehicle drivers with retail merchants, initially starting with fuel stations. Our objective is to offer the best fuel prices, closest stations, friendliest service, cleanest restrooms, tastiest coffee and so much more, direct to vehicle drivers. Pumped End Device (ped) is the IVI presentation layer"
AUTHOR = "Bernard Craddock"
HOMEPAGE = "https://github.com/bernardpumped/ped/tree/agl"
BUGTRACKER = "https://github.com/bernardpumped/ped/issues"
SECTION = "graphics"

LICENSE = "GPL-3.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=e49f4652534af377a713df3d9dec60cb"

SRCREV = "de1c4cf92bf629a0fff0d5462e2ba9575b77de67"
SRC_URI = "git://github.com/bernardpumped/ped.git;lfs=0;branch=next_main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "pumped_end_device"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "bernardpumped-ped-pumped-end-device"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = ""

inherit flutter-app
