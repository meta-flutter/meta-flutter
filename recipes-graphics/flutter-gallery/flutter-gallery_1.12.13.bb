SUMMARY = "Flutter Gallery Example"

LICENSE = "BSD-3-Clause"

DEPENDS = "flutter-sdk-native"

S = "${STAGING_DATADIR_NATIVE}/flutter/sdk"

do_compile() {

    cd ${S}/examples/flutter_gallery
    PATH=${STAGING_DATADIR_NATIVE}/flutter/sdk/bin:${PATH} flutter build bundle
}

do_install() {

    cd ${S}/examples/flutter_gallery/build/flutter_assets
    install -d ${D}${datadir}/flutter/examples/flutter_gallery
    cp -rv . ${D}${datadir}/flutter/examples/flutter_gallery
}

FILES_${PN} = "${datadir}/flutter/examples/flutter_gallery/*"
