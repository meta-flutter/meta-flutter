SUMMARY = "Flutter Plugin Test Application"
DESCRIPTION = "Flutter Plugin Test Application"
AUTHOR = "Joel Winarske"
HOMEPAGE = "https://github.com/meta-flutter/linux_plugins"
BUGTRACKER = "https://github.com/meta-flutter/linux_plugins/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=df6bd2163489eedcdea6b9406bcbe1dd"

DEPENDS += "\
    libserialport \
    "

RDEPENDS_${PN} += "\
    bluez5 \
    geoclue \
    liberation-fonts \
    libgpiod \
    libsecret \
    libserialport \
    networkmanager \
    xdg-user-dirs \
    "

SRC_URI = "git://github.com/meta-flutter/linux_plugins.git;lfs=1;branch=main;protocol=https;destsuffix=git"

SRCREV = "05e7fa5c0d910ac49dadcef5af09159a37ef5c3c"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "linux_plugins"
FLUTTER_APPLICATION_INSTALL_PREFIX = "flutter/examples"

inherit flutter-app
