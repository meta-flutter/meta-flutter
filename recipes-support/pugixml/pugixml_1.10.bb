LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=e794502c64d22460e31e72abad44ed2e"

PV = "1.10"
SRC_URI = "git://github.com/zeux/pugixml.git;tag=v${PV}"

S = "${WORKDIR}/git"

inherit cmake

EXTRA_OECMAKE_append = " \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_VERBOSE_MAKEFILE=TRUE \
    "

FILES_${PN}-dev  = " \
    ${includedir} \
    ${libdir}/libpugixml.a \
    ${libdir}/pkgconfig \
    ${libdir}/cmake/pugixml \
    "

BBCLASSEXTEND += "native nativesdk"
