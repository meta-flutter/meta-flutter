LICENSE = "CLOSED"

SRCREV = "v1.10"
SRC_URI = "git://github.com/zeux/pugixml.git"

S = "${WORKDIR}/git"

inherit cmake

EXTRA_OECMAKE_append = " \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_VERBOSE_MAKEFILE=TRUE \
    "

BBCLASSEXTEND += "native nativesdk"
