#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Rive Common"
HOMEPAGE = "https://pub.dev/packages/rive_common"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

S = "${WORKDIR}/git"

CARGO_SRC_DIR = "taffy_ffi"

inherit cargo

SRC_URI += " \
    git://github.com/meta-flutter/rive-common.git;protocol=https;lfs=0;nobranch=1;name=taffy_ffi \
    git://github.com/DioxusLabs/taffy;lfs=0;nobranch=1;protocol=https;destsuffix=taffy;name=taffy \
    crate://crates.io/arrayvec/0.7.4 \
    crate://crates.io/autocfg/1.1.0 \
    crate://crates.io/grid/0.11.0 \
    crate://crates.io/num-traits/0.2.18 \
    crate://crates.io/slotmap/1.0.7 \
    crate://crates.io/version_check/0.9.4 \
"

SRCREV_FORMAT .= "_taffy_ffi"
SRCREV_taffy = "9de5393c689e9e95e410d88a780772e42eb1e760"
SRCREV_FORMAT .= "_taffy"
SRCREV_taffy = "daa07e0f4e3e009f5b0c11ada5df9785efd4b2c2"
EXTRA_OECARGO_PATHS += "${WORKDIR}/taffy"

cargo_do_compile:prepend() {
    export RUSTFLAGS="-Clinker-plugin-lto"
}

FILES:${PN}-dev = "${libdir}"
