#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c2c05f9bdd5fc0b458037c2d1fb8d95e"

SRC_URI = "git://chromium.googlesource.com/chromium/tools/depot_tools;protocol=https;branch=main"

SRCREV = "39b2e4efd608584059aa5bb9af8e65597ca86276"

S = "${WORKDIR}/git"

inherit native

VPYTHON_VIRTUALENV_ROOT ??= "${WORKDIR}/.vpython-root"

do_configure[network] = "1"
do_configure() {
    export http_proxy=${http_proxy}
    export https_proxy=${https_proxy}

    cd ${S}
    export DEPOT_TOOLS_UPDATE=0
    export PATH=${S}:$PATH
    export VPYTHON_VIRTUALENV_ROOT=${VPYTHON_VIRTUALENV_ROOT}

    # Required since depot_tools_config_dir will return a path based on XDG_CONFIG_HOME
    export XDG_CONFIG_HOME=${WORKDIR}

    gclient --version
}

do_install() {

    install -d ${D}/${datadir}/depot_tools
    cp -rTv ${S}/. ${D}${datadir}/depot_tools
}

INSANE_SKIP:${PN} = "already-stripped"

# vim:set ts=4 sw=4 sts=4 expandtab:
