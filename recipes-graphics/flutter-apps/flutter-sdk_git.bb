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
SRCREV ??= "${@get_flutter_hash(d)}"

S = "${WORKDIR}/flutter"

common_compile() {

    export CURL_CA_BUNDLE=${STAGING_DIR_NATIVE}/etc/ssl/certs/ca-certificates.crt
    export PATH=${S}/bin:$PATH
    export PUB_CACHE=${S}/.pub-cache

    bbnote "Flutter SDK: ${FLUTTER_SDK_TAG}"

    flutter config --no-enable-android
    flutter config --no-enable-ios
    flutter config --no-enable-web
    flutter config --no-enable-linux-desktop
    flutter config --enable-custom-devices

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

def get_flutter_archive(d):
    return _get_flutter_release_info(d, "archive")

def get_flutter_sha256(d):
    return _get_flutter_release_info(d, "sha256")

def get_flutter_hash(d):
    return _get_flutter_release_info(d, "hash")

def _get_flutter_release_info(d, key):
    import urllib
    import json
    ret = ""
    flutter_sdk_tag = d.getVar("FLUTTER_SDK_TAG")
    with urllib.request.urlopen('https://storage.googleapis.com/flutter_infra_release/releases/releases_linux.json') as f:
        releases_linux_json = json.load(f)
        if flutter_sdk_tag == "AUTOINC":
            flutter_sdk_tag = releases_linux_json["current_release"]["dev"]

        releases = releases_linux_json["releases"]
        for r in releases:
            if r["version"] == flutter_sdk_tag or r["hash"] == flutter_sdk_tag:
                ret = r[key]
                break

    if ret == "":
        raise ValueError("Could not get flutter sdk archive url")

    return ret
