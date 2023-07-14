#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c2c05f9bdd5fc0b458037c2d1fb8d95e"

SRC_URI = "git://chromium.googlesource.com/chromium/tools/depot_tools;protocol=https;branch=main \
           file://0001-disable-ninjalog_upload.patch"

SRCREV = "64b61755572b1d03b3a43f1a29b617dcae3a3fe0"

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

INSANE_SKIP:${PN} = "already-stripped"

# vim:set ts=4 sw=4 sts=4 expandtab:
