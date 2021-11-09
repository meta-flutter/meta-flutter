SUMMARY = "Flutter makes it easy and fast to build beautiful apps for mobile and beyond."
DESCRIPTION = "Flutter is Google's SDK for crafting beautiful, fast user experiences for \
               mobile, web, and desktop from a single codebase. Flutter works with \
               existing code, is used by developers and organizations around the world, \
               and is free and open source."
AUTHOR = "Google"
HOMEPAGE = "https://flutter.dev/"
BUGTRACKER = "https://github.com/flutter/flutter/issues"
SECTION = "graphics"
CVE_PRODUCT = ""

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1d84cf16c48e571923f837136633a265"

FLUTTER_CHANNEL ??= "beta"

DEPENDS += " ca-certificates-native curl-native unzip-native"
RDEPENDS:${PN} += "ca-certificates-native curl-native unzip-native"
RDEPENDS:nativesdk-${PN} += "ca-certificates-native curl-native unzip-native"

SRC_URI = "git://github.com/flutter/flutter;protocol=https;branch=${FLUTTER_CHANNEL};name=repo"

SRCREV = "${AUTOREV}"

SRC_URI[certs.md5sum] = "1ecab07e89925a6e8684b75b8cf84890"

S = "${WORKDIR}/git"

inherit native


do_compile() {
    export CURL_CA_BUNDLE=${STAGING_DIR_NATIVE}/etc/ssl/certs/ca-certificates.crt
    export PATH=${S}/bin:$PATH
    export PUB_CACHE=${S}/.pub-cache

    bbnote "Using Flutter SDK Channel = ${FLUTTER_CHANNEL}"

    flutter config --no-enable-android
    flutter config --no-enable-ios
    flutter config --no-enable-web
    flutter config --enable-linux-desktop

    flutter channel ${FLUTTER_CHANNEL}
    flutter upgrade

    bbnote `flutter doctor -v`
}

do_install() {
    install -d ${D}${datadir}/flutter/sdk
    cp -rTv ${S}/. ${D}${datadir}/flutter/sdk
}


ALLOW_EMPTY:${PN} = "1"

FILES:${PN}-dev = "${datadir}/flutter/sdk"
FILES:${PN}-dev:class-nativesdk = "${datadir}/flutter/sdk"

INSANE_SKIP:${PN} += "already-stripped"

BBCLASSEXTEND = "native nativesdk"
