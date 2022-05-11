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

RDEPENDS:${PN} += "\
    bluez5 \
    geoclue \
    gsettings-desktop-schemas \
    liberation-fonts \
    libgpiod \
    libserialport \
    networkmanager \
    os-release \
    upower \
    xdg-user-dirs \
    "

SRC_URI = "git://github.com/meta-flutter/linux_plugins.git;lfs=1;branch=main;protocol=https;destsuffix=git \
           file://lsb-release"

SRCREV = "05e7fa5c0d910ac49dadcef5af09159a37ef5c3c"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "linux_plugins"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

inherit flutter-app

# Plugin Plus "Package Info" looks for "/usr/bin/data/flutter_assets/version.json".

# Plugin Plus "Device Info" requires "/etc/lsb-release"
do_install:append() {
    install -d ${D}${sysconfdir}
    install -m 644 ${WORKDIR}/lsb-release ${D}${sysconfdir}
    sed -i "s|@FLUTTER_SDK_TAG@|${FLUTTER_SDK_TAG}|g" ${D}${sysconfdir}/lsb-release
}

FILES:${PN} += "\
    ${sysconfdir}/lsb-release \
    "
