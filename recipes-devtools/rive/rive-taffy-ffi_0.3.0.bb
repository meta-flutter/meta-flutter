#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Rive Common"
HOMEPAGE = "https://pub.dev/packages/rive_common"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI += " \
    https://pub.dartlang.org/packages/rive_common/versions/0.3.0.tar.gz;downloadfilename=pub-dartlang-rive_common-0.3.0.tar.gz;subdir=src;name=taffy_ffi \
    file://update_cargo.patch \
"
SRC_URI[taffy_ffi.sha256sum] = "a6d05f65985e3ec18b7051ced6316d56374e0ca9c58288c37dd8510cb077832b"

S = "${WORKDIR}/src"

CARGO_SRC_DIR = "taffy_ffi"

inherit cargo

SRC_URI += " \
    git://github.com/DioxusLabs/taffy;lfs=0;nobranch=1;protocol=https;destsuffix=taffy;name=taffy \
    crate://crates.io/arrayvec/0.7.4 \
    crate://crates.io/autocfg/1.1.0 \
    crate://crates.io/grid/0.11.0 \
    crate://crates.io/num-traits/0.2.18 \
    crate://crates.io/slotmap/1.0.7 \
    crate://crates.io/version_check/0.9.4 \
"

SRCREV_FORMAT .= "_taffy"
SRCREV_taffy = "daa07e0f4e3e009f5b0c11ada5df9785efd4b2c2"
EXTRA_OECARGO_PATHS += "${WORKDIR}/taffy"


cargo_do_compile:prepend() {
    export RUSTFLAGS="-Clinker-plugin-lto --emit=llvm-ir"
}

do_install() {
    install -d ${D}${libdir}/taffy_ffi
    install ${B}/target/*/release/libtaffy_ffi.a ${D}${libdir}/taffy_ffi/
}

FILES:${PN}-staticdev = "${libdir}"
