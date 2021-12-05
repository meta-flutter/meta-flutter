# Helper class for building Flutter Application.
# Assumes that:
# - Flutter Application does not have a linux folder.  If it does it 
#   will incorrectly link to Linux GTK embedder.  We don't want that.
# - S is defined and points to source directory.
# - PUBSPEC_APPNAME is defined correctly.  This is the name value from pubspec.yml.
#   TODO -> read pubspec.yml via python

DEPENDS += " \
    ca-certificates-native \
    flutter-engine \
    flutter-sdk-native \
    unzip-native \
    "

#
# Extract Engine SDK
#

do_configure() {

    # Engine SDK
    rm -rf ${S}/engine_sdk
    unzip ${STAGING_DATADIR}/flutter/engine_sdk.zip -d ${S}/engine_sdk
}

#
# Build flutter_assets folder and AOT (libapp.so)
#

do_compile() {

    FLUTTER_SDK=${STAGING_DIR_NATIVE}/usr/share/flutter/sdk
    ENGINE_SDK=${S}/engine_sdk/sdk

    export PATH=${FLUTTER_SDK}/bin:$PATH

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

    ${ENGINE_SDK}/clang_x64/gen_snapshot \
      --snapshot_kind=app-aot-elf \
      --elf=libapp.so \
      --strip \
      .dart_tool/flutter_build/*/app.dill
}

INSANE_SKIP_${PN} += " ldflags"
SOLIBS = ".so"
FILES_SOLIBSDEV = ""

do_install() {
    install -d ${D}${datadir}/${PUBSPEC_APPNAME}
    cp ${S}/libapp.so ${D}${datadir}/${PUBSPEC_APPNAME}/
    cp -r ${S}/build/flutter_assets/* ${D}${datadir}/${PUBSPEC_APPNAME}/
}

FILES_${PN} = "${datadir}"
FILES_${PN}-dev = ""
