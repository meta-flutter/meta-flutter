SUMMARY = "HVAC flutter demo application"
DESCRIPTION = "HVAC flutter demo application"
AUTHOR = "Lorenzo Tilve"
HOMEPAGE = "https://github.com/ltilve/agl-flutter-hvac"
BUGTRACKER = "https://github.com/ltilve/agl-flutter-hvac/issues"
SECTION = "graphics"
CVE_PRODUCT = ""

LICENSE = "CLOSED"

DEPENDS += "flutter-engine flutter-sdk-native unzip-native"

SRC_URI = "git://github.com/ltilve/agl-flutter-hvac.git;lfs=0;protocol=https;destsuffix=git"

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
    
    ${ENGINE_SDK}/clang_x64/dart ${FLUTTER_SDK}/bin/cache/dart-sdk/bin/snapshots/frontend_server.dart.snapshot \
      --aot --tfa --target=flutter \
      --sdk-root ${FLUTTER_SDK}/bin/cache/artifacts/engine/common/flutter_patched_sdk \
      --output-dill app.dill \
      lib/main.dart
      
    ${ENGINE_SDK}/clang_x64/gen_snapshot \
      --deterministic \
      --snapshot_kind=app-aot-elf \
      --elf=libapp.so \
      --strip app.dill
}

do_install() {

    #
    # Toyota Layout
    #

    install -d ${D}${datadir}/homescreen/hvac
    cp -r ${S}/build/* ${D}${datadir}/homescreen/hvac
    rm -rf ${D}${datadir}/homescreen/gallery/.last_build_id
    install -m 644 ${S}/libapp.so ${D}${datadir}/homescreen/hvac/libapp.so

    # flutter application path that ivi-homescreen loads
    cd ${D}${datadir}/homescreen
    ln -sf hvac/ bundle/
}

FILES_${PN} = "${datadir}/homescreen \
               ${datadir}/${PN} \
              "

do_package_qa[noexec] = "1"
