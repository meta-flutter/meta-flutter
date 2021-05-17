SUMMARY = "Flutter Gallery Application"
DESCRIPTION = "Flutter Gallery Application"
AUTHOR = "Google"
HOMEPAGE = "https://github.com/flutter/gallery"
BUGTRACKER = "https://github.com/flutter/gallery/issues"
SECTION = "graphics"
CVE_PRODUCT = ""

LICENSE = "CLOSED"

FLUTTER_CHANNEL ??= "beta"

DEPENDS += "flutter-engine flutter-sdk-native unzip-native"

SRC_URI = "git://github.com/flutter/gallery.git;lfs=0;protocol=https;destsuffix=git"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

do_patch() {
    export CURL_CA_BUNDLE=${STAGING_DIR_NATIVE}/etc/ssl/certs/ca-certificates.crt
    export PATH=${STAGING_DIR_NATIVE}/usr/share/flutter/sdk/bin:$PATH

    FLUTTER_VER="$( flutter --version | head -n 1 | awk '{print $2}' )"
    echo "Flutter Version: ${FLUTTER_VER}"
}
do_patch[depends] += "flutter-sdk-native:do_populate_sysroot"

do_configure() {
    #
    # Engine SDK
    #
    rm -rf ${S}/engine_sdk
    unzip ${STAGING_DATADIR}/flutter/engine_sdk.zip -d ${S}/engine_sdk
}

do_compile() {
    export PATH=${STAGING_DIR_NATIVE}/usr/share/flutter/sdk/bin:$PATH

    ENGINE_SDK=${S}/engine_sdk/sdk

    cd ${S}
    flutter build bundle
    dart ${ENGINE_SDK}/frontend_server.dart.snapshot --aot --tfa --target=flutter --sdk-root ${ENGINE_SDK} --output-dill app.dill lib/main.dart
    ${ENGINE_SDK}/clang_x64/gen_snapshot --deterministic --snapshot_kind=app-aot-elf --elf=build/libapp.so --strip app.dill
}

do_install() {
    install -d ${D}${datadir}/homescreen/gallery
    cp -rTv ${S}/ivi/build ${D}${datadir}/homescreen/gallery
    rm -rf ${D}${datadir}/homescreen/gallery/.last_build_id
}

PACKAGES =+ "\
    ${PN}-gallery \
    "

FILES_${PN}-ivi = "${datadir}/homescreen/gallery/*"

do_package_qa[noexec] = "1"
