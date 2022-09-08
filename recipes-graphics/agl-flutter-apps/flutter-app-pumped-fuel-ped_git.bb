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

SRCREV = "bee63b8317fbf8f6b95d6be820bcf07b6b3c0c37"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "pumped_end_device"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

FLUTTER_BUILD_ARGS = "bundle"

inherit flutter-app
