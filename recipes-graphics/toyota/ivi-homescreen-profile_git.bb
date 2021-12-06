require ivi-homescreen.inc

FLUTTER_RUNTIME = "profile"

DEPENDS += "flutter-engine-${FLUTTER_RUNTIME}"

SERVICE_EXEC_START = "/usr/bin/homescreen --f --observatory-host 0.0.0.0 --observatory-port 1234 --start-paused"
