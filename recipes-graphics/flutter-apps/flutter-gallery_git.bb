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


def get_pubspec_value(variable):
    """
    Opens pubspec yaml file, returns
    value of selected variable
    """
    try:
        with open('pubspec.yml', 'r') as file_data:
            yaml_data = file_data.read()
    except URLError as e:
        print(e)

    # Parse the YAML
    try:
        pubspec_data = yaml.safe_load(yaml_data)

        return pubspec_data[variable]
    except yaml.YAMLError as e:
        print(e)


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
    PUBSPEC_APPNAME=gallery

    export PATH=${FLUTTER_SDK}/bin:$PATH

    bb.note("${@get_pubspec_value('name')}")
    bb.note("${@get_pubspec_value('description')}")
    bb.note("${@get_pubspec_value('version')}")

    cd ${S}

    flutter build bundle

    ${FLUTTER_SDK}/bin/cache/dart-sdk/bin/dart \
      --verbose \
      --disable-analytics \
      --disable-dart-dev ${FLUTTER_SDK}/bin/cache/artifacts/engine/linux-x64/frontend_server.dart.snapshot \
      --sdk-root ${FLUTTER_SDK}/bin/cache/artifacts/engine/common/flutter_patched_sdk_product/ \
      --target=flutter \
      --no-print-incremental-dependencies \
      -Ddart.vm.profile=false -Ddart.vm.product=true \
      --aot --tfa \
      --packages .dart_tool/package_config.json \
      --output-dill .dart_tool/flutter_build/*/app.dill \
      --depfile .dart_tool/flutter_build/*/kernel_snapshot.d \
      package:${PUBSPEC_APPNAME}/main.dart

    install -d ${D}${datadir}/homescreen/gallery

    ${ENGINE_SDK}/clang_x64/gen_snapshot \
      --snapshot_kind=app-aot-elf \
      --elf=${D}${datadir}/homescreen/gallery/libapp.so \
      --strip \
      .dart_tool/flutter_build/*/app.dill
}

do_install() {

    PUBSPEC_APPNAME=gallery

    #
    # Toyota
    #

    install -d ${D}${datadir}/homescreen/${PUBSPEC_APPNAME}

    cp -r ${S}/build/flutter_assets ${D}${datadir}/homescreen/${PUBSPEC_APPNAME}

    # set flutter application to run
    ln -sf ${datadir}/homescreen/${PUBSPEC_APPNAME}/ ${D}${datadir}/homescreen/bundle

    #
    # Flutter PI
    #
    ln -sf ${datadir}/homescreen/${PUBSPEC_APPNAME}/flutter_assets/libapp.so \
      ${D}${datadir}/homescreen/${PUBSPEC_APPNAME}/flutter_assets/app.so

    #
    # Sony
    #
    install -d ${D}${datadir}/${PN}/sony    
    install -d ${D}${datadir}/${PN}/sony/lib
    install -d ${D}${datadir}/${PN}/sony/data

    ln -sf ${datadir}/homescreen/${PUBSPEC_APPNAME}/flutter_assets/libapp.so \
      ${D}${datadir}/${PN}/sony/lib/libapp.so
   
    ln -sf ${datadir}/homescreen/${PUBSPEC_APPNAME}/flutter_assets \
      ${D}${datadir}/${PN}/sony/data/flutter_assets

    ln -sf ${datadir}/flutter/icudtl.dat \
      ${D}${datadir}/${PN}/sony/data/
}

FILES:${PN} = "${datadir}/homescreen \
               ${datadir}/${PN} \
              "

do_package_qa[noexec] = "1"
