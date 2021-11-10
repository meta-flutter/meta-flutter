SUMMARY = "Flutter Gallery Application"
DESCRIPTION = "Flutter Gallery Application"
AUTHOR = "Google"
HOMEPAGE = "https://github.com/flutter/gallery"
BUGTRACKER = "https://github.com/flutter/gallery/issues"
SECTION = "graphics"
CVE_PRODUCT = ""

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3ac21e3d8ebe7dd79f273ca11b9e7b4e"

DEPENDS += "flutter-engine flutter-sdk-native unzip-native"

SRC_URI = "git://github.com/flutter/gallery.git;lfs=0;protocol=https;destsuffix=git"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

do_patch() {
    export CURL_CA_BUNDLE=${STAGING_DIR_NATIVE}/usr/share/depot_tools/ca-certificates.crt
    export PATH=${STAGING_DIR_NATIVE}/usr/share/flutter/sdk/bin:$PATH
    export PUB_CACHE=${STAGING_DIR_NATIVE}/usr/share/flutter/sdk/.pub-cache

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
    FLUTTER_SDK=${STAGING_DIR_NATIVE}/usr/share/flutter/sdk
    ENGINE_SDK=${S}/engine_sdk/sdk

    export PATH=${FLUTTER_SDK}/bin:$PATH

    cd ${S}
    flutter config --no-enable-android
    flutter config --no-enable-ios
    flutter config --no-enable-web
    flutter config --enable-linux-desktop
    flutter create .
    flutter build bundle
}

do_install() {

    #
    # Toyota Layout
    #

    install -d ${D}${datadir}/homescreen/gallery
    cp -r ${S}/build/* ${D}${datadir}/homescreen/gallery
    rm -rf ${D}${datadir}/homescreen/gallery/.last_build_id

    # set flutter application to run
    cd ${D}${datadir}/homescreen
    ln -sf gallery/ bundle
    
    #
    # Sony Layout
    #
    install -d ${D}${datadir}/${PN}/sony    
    install -d ${D}${datadir}/${PN}/sony/lib
    install -m 644 ${S}/libapp.so ${D}${datadir}/${PN}/sony/lib
    
    install -d ${D}${datadir}/${PN}/sony/data
    install -m 644 ${STAGING_DATADIR}/flutter/icudtl.dat ${D}${datadir}/${PN}/sony/data/
    
    install -d ${D}${datadir}/${PN}/sony/data/flutter_assets
    cp -rTv ${S}/build/flutter_assets/. ${D}${datadir}/${PN}/sony/data/flutter_assets/
}

FILES_${PN} = "${datadir}/homescreen \
               ${datadir}/${PN} \
              "

do_package_qa[noexec] = "1"
