#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c2c05f9bdd5fc0b458037c2d1fb8d95e"

SRC_URI = "git://chromium.googlesource.com/chromium/tools/depot_tools;protocol=https;branch=main"

SRCREV = "120efcb475aa5f8c6f38c4598c602f4713015112"

S = "${WORKDIR}/git"

inherit native

do_configure[network] = "1"
do_configure() {
    export http_proxy=${http_proxy}
    export https_proxy=${https_proxy}

    cd ${S}
    export DEPOT_TOOLS_UPDATE=0
    export PATH=${S}:$PATH

    gclient --version
}

do_install() {

    install -d ${D}/${datadir}/depot_tools
    cp -rTv ${S}/. ${D}${datadir}/depot_tools
}

INSANE_SKIP:${PN} = "already-stripped"

# vim:set ts=4 sw=4 sts=4 expandtab:
