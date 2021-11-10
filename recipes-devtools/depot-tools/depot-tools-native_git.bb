LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c2c05f9bdd5fc0b458037c2d1fb8d95e"

RDEPENDS:${PN}-dev += "ca-certificates bash perl perl-modules python3"
RDEPENDS:${PN}-native-dev += "ca-certificates-native bash-native perl-native python3-native"

SRC_URI = "git://chromium.googlesource.com/chromium/tools/depot_tools;protocol=https;branch=main \
           file://0001-disable-ninjalog_upload.patch"

SRCREV = "3a56ba9d9c9d22bc78e24f96a9096247d53649f8"

S = "${WORKDIR}/git"


do_install() {

    install -d ${D}/${datadir}/depot_tools
    cp -rTv ${S}/. ${D}${datadir}/depot_tools

    rm ${D}${datadir}/depot_tools/ninja-linux32
    rm ${D}${datadir}/depot_tools/ninja-linux64
}


BBCLASSEXTEND = "native nativesdk"

ALLOW_EMPTY:${PN} = "1"

FILES:${PN}-dev = "${datadir}/depot_tools"
FILES:${PN}-dev:class-nativesdk = "${datadir}/depot_tools"

INSANE_SKIP:${PN}-dev += "already-stripped file-rdeps"

BBCLASSEXTEND = "native nativesdk"
