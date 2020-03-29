
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI += "file://ca-certificates.crt"

do_install() {
    install -d ${D}${sysconfdir}/ssl/certs
    install -m 644 ${WORKDIR}/ca-certificates.crt ${D}${sysconfdir}/ssl/certs
}

SYSROOT_DIRS += "${sysconfdir}/ssl/certs"

FILES_${PN}-dev = "${sysconfdir}/ssl/certs/ca-certificates.crt"
