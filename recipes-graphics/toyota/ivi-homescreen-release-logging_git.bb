require ivi-homescreen.inc

FLUTTER_RUNTIME = "release"

EXTRA_OECMAKE += "-D CMAKE_BUILD_TYPE=Debug"

RDEPENDS_${PN} += "flutter-engine-${FLUTTER_RUNTIME}"
