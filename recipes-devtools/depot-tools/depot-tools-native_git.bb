LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c2c05f9bdd5fc0b458037c2d1fb8d95e"

SRC_URI = "git://chromium.googlesource.com/chromium/tools/depot_tools;protocol=https;branch=main \
           file://0001-disable-ninjalog_upload.patch"

SRCREV = "3a56ba9d9c9d22bc78e24f96a9096247d53649f8"

S = "${WORKDIR}/git"

inherit native

do_compile[network] = "1"
do_compile() {

    # force bootstrap download to get python2
    
    cd ${S}
    export DEPOT_TOOLS_UPDATE=0
    export GCLIENT_PY3=1
    export PATH=${S}:$PATH

    gclient --version
}

do_install() {

    install -d ${D}/${datadir}/depot_tools
    cp -rTv ${S}/. ${D}${datadir}/depot_tools
}

FILES:${PN}-dev = "${datadir}/depot_tools/*"

INSANE_SKIP:${PN}-dev = "already-stripped"

BBCLASSEXTEND += "native"

# vim:set ts=4 sw=4 sts=4 expandtab:
