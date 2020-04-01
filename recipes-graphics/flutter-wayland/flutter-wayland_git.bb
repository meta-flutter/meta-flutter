LICENSE = "CLOSED"

SRCREV = "7cbac5f2efdffffc53cdc929b5c48bf602e18a16"
SRC_URI = "git://github.com/jwinarske/flutter_wayland;protocol=https;branch=yocto_zeus"

DEPENDS += " flutter-engine wayland mesa"

REQUIRED_DISTRO_FEATURES = "wayland"


S = "${WORKDIR}/git"

inherit cmake

EXTRA_OECMAKE = " \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_VERBOSE_MAKEFILE=TRUE \
    -DFLUTTER_ENGINE_LIBRARY=${STAGING_LIBDIR}/flutter/libflutter_engine.so \
    "

FILES_${PN}  = " \
    ${bindir}/flutter_wayland \
    "
