SUMMARY = "Minimum Viable Product (MVP) Wayland compositor"
DESCRIPTION = "This is the \"minimum viable product\" Wayland compositor based on wlroots."
AUTHOR = ""
HOMEPAGE = "https://github.com/swaywm/wlroots"
BUGTRACKER = "https://github.com/swaywm/wlroots/issues"
SECTION = "graphics"
LICENSE = "MIT"
CVE_PRODUCT = "tinywl"

LIC_FILES_CHKSUM = "file://LICENSE;md5=7578fad101710ea2d289ff5411f1b818"

DEPENDS += "wayland-native wlroots"

SRC_URI = "git://github.com/swaywm/wlroots.git;protocol=https;nobranch=1 \
           file://0001-tinywl_ldflags.patch"
SRCREV = "238d1c078fb03338e9f271d98f7bf6b1fc399285"

S = "${WORKDIR}/git"

do_compile_prepend() {
    cd ${S}/tinywl
}

do_install() {
    install -d ${D}${bindir}
    install -m 755 tinywl/tinywl ${D}${bindir}
}

FILES_${PN} = "${bindir}/tinywl"

BBCLASSEXTEND = ""
