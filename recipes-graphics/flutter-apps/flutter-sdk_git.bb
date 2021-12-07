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

FLUTTER_CHANNEL ??= "dev"

DEPENDS += " ca-certificates-native curl-native unzip-native"
RDEPENDS_${PN}-native += "ca-certificates-native curl-native perl perl-modules unzip-native"
RDEPENDS_nativesdk-${PN} += "ca-certificates-native curl-native perl perl-modules unzip-native"

SRC_URI = "git://github.com/flutter/flutter;protocol=https;branch=${FLUTTER_CHANNEL};name=repo"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"


common_compile() {

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

do_compile_class-native() {
    common_compile
}

do_compile_class-nativesdk() {
    common_compile
}

do_install_class-native() {
    install -d ${D}${datadir}/flutter/sdk
    cp -rTv ${S}/. ${D}${datadir}/flutter/sdk
}
do_install_class-nativesdk() {
    install -d ${D}${datadir}/flutter/sdk
    cp -rTv ${S}/. ${D}${datadir}/flutter/sdk
}


ALLOW_EMPTY_${PN} = "1"

FILES_${PN} = "${datadir}/flutter/sdk"

INSANE_SKIP_${PN} += "already-stripped file-rdeps"

BBCLASSEXTEND = "native nativesdk"
