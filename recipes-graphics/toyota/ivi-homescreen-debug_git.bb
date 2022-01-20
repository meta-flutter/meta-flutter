require ivi-homescreen.inc

FLUTTER_RUNTIME = "debug"

RDEPENDS_${PN} += "flutter-engine-${FLUTTER_RUNTIME}"

SERVICE_EXEC_START = "/usr/bin/homescreen --f --observatory-host 0.0.0.0 --observatory-port 1234"

# Enable Verbose Logging
EXTRA_OECMAKE += "-D CMAKE_BUILD_TYPE=Debug"

FILES_${PN} += "${datadir}"
