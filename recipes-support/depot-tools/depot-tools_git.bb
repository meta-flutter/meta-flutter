LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c2c05f9bdd5fc0b458037c2d1fb8d95e"

FILESEXTRAPATHS_prepend_poky := "${THISDIR}/files:"

SRCREV = "6a7e234b584eff3fbbd5686f5ec75cba3d25667c"
SRC_URI = "git://chromium.googlesource.com/chromium/tools/depot_tools;protocol=https \
           file://ca-certificates.crt;name=certs"

SRC_URI[certs.md5sum] = "1ecab07e89925a6e8684b75b8cf84890"

S = "${WORKDIR}/git"

do_install() {

    install -d ${D}/${bindir}/depot_tools
    cp -r ${S}/* ${D}${bindir}/depot_tools

    install -m 644 ${WORKDIR}/ca-certificates.crt ${D}${bindir}/depot_tools
}

SYSROOT_DIRS =+ "${bindir}"

FILES_${PN}-dev = "${bindir}/depot_tools/*"

INSANE_SKIP_${PN} = "already-stripped"

BBCLASSEXTEND =+ "native nativesdk"

# vim:set ts=4 sw=4 sts=4 expandtab:
