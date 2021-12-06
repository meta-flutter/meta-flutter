require ivi-homescreen.inc

FLUTTER_RUNTIME = "debug"

DEPENDS += "flutter-engine-${FLUTTER_RUNTIME}"

SERVICE_EXEC_START = "/usr/bin/homescreen --f --observatory-host 0.0.0.0 --observatory-port 1234"

# Verbose Logging
EXTRA_OECMAKE += "-D CMAKE_BUILD_TYPE=Debug"
