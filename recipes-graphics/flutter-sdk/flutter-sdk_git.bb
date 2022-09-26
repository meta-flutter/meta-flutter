SUMMARY = "Flutter makes it easy and fast to build beautiful apps for mobile and beyond."
DESCRIPTION = "Flutter is Google's SDK for crafting beautiful, fast user experiences for \
               mobile, web, and desktop from a single codebase. Flutter works with \
               existing code, is used by developers and organizations around the world, \
               and is free and open source."
AUTHOR = "Google"
HOMEPAGE = "https://flutter.dev/"
BUGTRACKER = "https://github.com/flutter/flutter/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1d84cf16c48e571923f837136633a265"

DEPENDS += "\
    ca-certificates-native \
    curl-native \
    unzip-native \
    "

RDEPENDS_${PN}-native += "ca-certificates-native curl-native perl perl-modules unzip-native"
RDEPENDS_nativesdk-${PN} += "ca-certificates-native curl-native perl perl-modules unzip-native"

require conf/include/flutter-version.inc

SRC_URI = "https://storage.googleapis.com/flutter_infra_release/releases/${@get_flutter_archive(d)};name=flutter-sdk"
SRC_URI[flutter-sdk.sha256sum] = "${@get_flutter_sha256(d)}"

S = "${WORKDIR}/flutter"

do_unpack[network] = "1"
do_patch[network] = "1"
do_compile[network] = "1"

common_compile() {

    export CURL_CA_BUNDLE=${STAGING_DIR_NATIVE}/etc/ssl/certs/ca-certificates.crt
    export PATH=${S}/bin:$PATH
    export PUB_CACHE=${S}/.pub-cache

    export http_proxy=${http_proxy}
    export https_proxy=${https_proxy}

    bbnote "Flutter SDK: ${FLUTTER_SDK_TAG}"

    flutter config --clear-features
    flutter config --enable-linux-desktop
    flutter config --enable-custom-devices
    flutter config --no-analytics
    dart --disable-analytics

    bbnote `flutter config`
    bbnote `flutter doctor -v`
}

do_compile_class-native() {
    common_compile
}

do_compile_class-nativesdk() {
    common_compile
}

common_install() {
    install -d ${D}${datadir}/flutter/sdk
    cp -rTv ${S}/. ${D}${datadir}/flutter/sdk
    rm -rf ${D}${datadir}/flutter/sdk/bin/cache/artifacts/*
}

do_install_class-native() {
    common_install
}
do_install_class-nativesdk() {
    common_install
}


ALLOW_EMPTY_${PN} = "1"

FILES_${PN} = "${datadir}"

INSANE_SKIP_${PN} += "already-stripped file-rdeps"

BBCLASSEXTEND = "native nativesdk"
