#
# Copyright (c) 2020-2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "keyboard_test"
DESCRIPTION = "Integration test app for the ivi-homescreen keyboard pipeline, \
covering text entry, IME composition and emoji rendering."

require ivi-homescreen-test.inc

PUBSPEC_APPNAME = "keyboard_test"

# The pubspec declares three font assets under fonts/, and the repository does
# not ship them: .gitignore excludes the directory and fetch_fonts.sh downloads
# them on a developer machine before `emb bundle`. A Flutter build fails on a
# declared asset that is absent, so the fonts have to come from SRC_URI instead
# -- do_compile has no network, and giving it one would defeat the checksums.
#
# The revisions and hashes are the ones fetch_fonts.sh pins, so this fetches
# exactly what a developer build would.
SRC_URI += "\
    https://github.com/dejavu-fonts/dejavu-fonts/releases/download/version_2_37/dejavu-fonts-ttf-2.37.zip;name=dejavu;subdir=ihs-fonts \
    https://github.com/googlefonts/noto-emoji/raw/v2.047/fonts/Noto-COLRv1.ttf;name=notoemoji;downloadfilename=NotoColorEmoji.ttf;subdir=ihs-fonts \
    https://github.com/googlefonts/noto-emoji/raw/v2.047/LICENSE;name=notolicense;downloadfilename=LICENSE.noto-emoji;subdir=ihs-fonts \
    "

SRC_URI[dejavu.sha256sum] = "7576310b219e04159d35ff61dd4a4ec4cdba4f35c00e002a136f00e96a908b0a"
SRC_URI[notoemoji.sha256sum] = "23549f29b5ad741fcb4c025b8dc44652ff0f459892467ebcccec1e6bbe839b44"
SRC_URI[notolicense.sha256sum] = "500bb1ccf43df7bbb522112f9133a52b16e1c35e809632f5d8609b179152de5b"

# Bundling the fonts puts their licenses in the image, so both join the
# embedder's Apache-2.0: DejaVu is Bitstream-Vera plus public-domain additions,
# and Noto Color Emoji is OFL-1.1.
LICENSE = "Apache-2.0 & Bitstream-Vera & OFL-1.1"
LIC_FILES_CHKSUM = "\
    file://LICENSE;md5=39ae29158ce710399736340c60147314 \
    file://${UNPACKDIR}/ihs-fonts/dejavu-fonts-ttf-2.37/LICENSE;md5=449b2c30bfe5fa897fe87b8b70b16cfa \
    file://${UNPACKDIR}/ihs-fonts/LICENSE.noto-emoji;md5=cdc5040ed1e8cf5d3516f5285fd7b636 \
    "

# fetch_fonts.sh lands these in the app's fonts/ under the names the pubspec
# asks for; Noto ships as Noto-COLRv1.ttf and is renamed on download.
do_configure:prepend() {
    install -d ${S}/${FLUTTER_APPLICATION_PATH}/fonts
    install -m 0644 ${UNPACKDIR}/ihs-fonts/dejavu-fonts-ttf-2.37/ttf/DejaVuSans.ttf \
        ${S}/${FLUTTER_APPLICATION_PATH}/fonts/DejaVuSans.ttf
    install -m 0644 ${UNPACKDIR}/ihs-fonts/dejavu-fonts-ttf-2.37/ttf/DejaVuSansMono.ttf \
        ${S}/${FLUTTER_APPLICATION_PATH}/fonts/DejaVuSansMono.ttf
    install -m 0644 ${UNPACKDIR}/ihs-fonts/NotoColorEmoji.ttf \
        ${S}/${FLUTTER_APPLICATION_PATH}/fonts/NotoColorEmoji.ttf
}
