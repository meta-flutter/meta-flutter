SUMMARY = "Flutter Plugin Test Application"
DESCRIPTION = "Flutter Plugin Test Application"
AUTHOR = "Joel Winarske"
HOMEPAGE = "https://github.com/meta-flutter/linux_plugins"
BUGTRACKER = "https://github.com/meta-flutter/linux_plugins/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=df6bd2163489eedcdea6b9406bcbe1dd"

# Flutter Linux Plugins do not cross compile, hence -native reference
DEPENDS += "\
    jsoncpp \
    libsecret \
    "

RDEPENDS:${PN} += "\
    bluez5 \
    geoclue \
    gsettings-desktop-schemas \
    liberation-fonts \
    libgpiod \
    libsecret \
    libserialport \
    networkmanager \
    os-release \
    upower \
    xdg-user-dirs \
    "

SRCREV = "ecf66fe5511d8f329d38417d8407e67eb21c6638"
SRC_URI = "git://github.com/meta-flutter/linux_plugins.git;lfs=1;branch=main;protocol=https;destsuffix=git \
           file://lsb-release"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "linux_plugins"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

FLUTTER_BUILD_ARGS = "bundle --no-pub -v"
#FLUTTER_APP_DISABLE_NATIVE_PLUGINS = "1"
#FLUTTER_REMOVE_LINUX_BUILD_ARTIFACTS = "1"


inherit flutter-app

require conf/include/flutter-version.inc

# Plugin Plus "Package Info" looks for "/usr/bin/data/flutter_assets/version.json".

# Plugin Plus "Device Info" requires "/etc/lsb-release" which is not present with Poky
do_install:append() {
    install -d ${D}${sysconfdir}
    install -m 644 ${WORKDIR}/lsb-release ${D}${sysconfdir}
    sed -i "s|@FLUTTER_SDK_TAG@|${@get_flutter_sdk_version(d)}|g" ${D}${sysconfdir}/lsb-release
}

FILES:${PN} += "\
    ${sysconfdir}/lsb-release \
    "
