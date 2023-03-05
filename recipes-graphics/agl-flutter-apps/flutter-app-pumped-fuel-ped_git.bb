#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Pumped End Device"
DESCRIPTION = "Pumped is a combination of 3 core projects to connect \
               vehicle drivers with retail merchants, initially starting \
               with fuel stations. Our objective is to offer the best fuel \
               prices, closest stations, friendliest service, cleanest restrooms, \
               tastiest coffee and so much more, direct to vehicle drivers."
AUTHOR = "Pumped Fuel"
HOMEPAGE = "https://github.com/bernardpumped/ped"
BUGTRACKER = "https://github.com/bernardpumped/ped/issues"
SECTION = "graphics"

RDEPENDS:${PN} += "\
    geoclue \
    libsecret \
    xdg-user-dirs \
    "

LICENSE = "GPL-3.0"
LIC_FILES_CHKSUM = "file://COPYING;md5=e49f4652534af377a713df3d9dec60cb"

SRC_URI = "git://github.com/bernardpumped/ped.git;protocol=https;branch=agl"

SRCREV = "58f762e9e33002af5e6806f54f513fe5f24cc715"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "pumped_end_device"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

FLUTTER_BUILD_ARGS = "bundle"

inherit flutter-app
