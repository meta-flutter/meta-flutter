require ivi-homescreen.inc

FLUTTER_RUNTIME = "debug"

RDEPENDS:${PN} += "flutter-engine-${FLUTTER_RUNTIME}"

# Enable Verbose Logging
EXTRA_OECMAKE += "-D CMAKE_BUILD_TYPE=Debug"

FILES:${PN} += "${datadir}"
