LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=4fcc629d7a228e5e3e54c2fa1d73f774"

SRCREV = "467798271eca9f6fee05a70229606956127cb6c9"
SRC_URI = "git://github.com/jwinarske/flutter_wayland;protocol=https;branch=waylandpp"

DEPENDS =+ " flutter-engine wayland mesa waylandpp libxkbcommon rapidjson"

RDEPENDS_${PN} = "flutter-engine wayland mesa freetype waylandpp libxkbcommon rapidjson"

REQUIRED_DISTRO_FEATURES = "wayland"

S = "${WORKDIR}/git"

inherit cmake

EXTRA_OECMAKE = " \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_VERBOSE_MAKEFILE=TRUE \
    -DFLUTTER_ENGINE_LIBRARY=${STAGING_LIBDIR}/libflutter_engine.so \
    "

FILES_${PN}  = "${bindir}/flutter_wayland"
