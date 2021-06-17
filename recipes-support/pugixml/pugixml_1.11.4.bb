LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=e794502c64d22460e31e72abad44ed2e"

SRC_URI = "git://github.com/zeux/pugixml.git;protocol=https"
SRCREV = "08b3433180727ea2f78fe02e860a08471db1e03c"

S = "${WORKDIR}/git"

inherit cmake

EXTRA_OECMAKE_append = " \
    -DCMAKE_VERBOSE_MAKEFILE=TRUE \
    "

FILES_${PN}-dev  = " \
    ${includedir} \
    ${libdir}/libpugixml.a \
    ${libdir}/pkgconfig \
    ${libdir}/cmake/pugixml \
    "

BBCLASSEXTEND += "native nativesdk"
