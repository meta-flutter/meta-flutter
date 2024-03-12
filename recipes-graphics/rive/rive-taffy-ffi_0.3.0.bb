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
SRCREV_taffy_ffi = "08cdd32c85b777b8ec6f8643cfab41446b180426"
SRCREV_FORMAT .= "_taffy"
SRCREV_taffy = "daa07e0f4e3e009f5b0c11ada5df9785efd4b2c2"

SRC_URI[arrayvec-0.7.4.sha256sum] = "96d30a06541fbafbc7f82ed10c06164cfbd2c401138f6addd8404629c4b16711"
SRC_URI[autocfg-1.1.0.sha256sum] = "d468802bab17cbc0cc575e9b053f41e72aa36bfa6b7f55e3529ffa43161b97fa"
SRC_URI[grid-0.11.0.sha256sum] = "1df00eed8d1f0db937f6be10e46e8072b0671accb504cf0f959c5c52c679f5b9"
SRC_URI[num-traits-0.2.18.sha256sum] = "da0df0e5185db44f69b44f26786fe401b6c293d1907744beaa7fa62b2e5a517a"
SRC_URI[slotmap-1.0.7.sha256sum] = "dbff4acf519f630b3a3ddcfaea6c06b42174d9a44bc70c620e9ed1649d58b82a"
SRC_URI[version_check-0.9.4.sha256sum] = "49874b5167b65d7193b8aba1567f5c7d93d001cafc34600cee003eda787e483f"

EXTRA_OECARGO_PATHS += "${WORKDIR}/taffy"

RUSTFLAGS += " -Clink-arg=-Wl,-soname=taffy_ffi.so.${PV}"

FILES:${PN} = "${libdir}"
