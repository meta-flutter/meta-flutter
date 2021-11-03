LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=0618cf07b3350ec269695b32b835c1b2"

SRC_URI = "git://github.com/zeux/pugixml.git;protocol=https"
SRCREV = "08b3433180727ea2f78fe02e860a08471db1e03c"

S = "${WORKDIR}/git"

inherit cmake

EXTRA_OECMAKE:append = " \
    -DCMAKE_VERBOSE_MAKEFILE=TRUE \
    "

FILES:${PN}-dev  = " \
    ${includedir} \
    ${libdir}/libpugixml.a \
    ${libdir}/pkgconfig \
    ${libdir}/cmake/pugixml \
    "

BBCLASSEXTEND += "native nativesdk"
