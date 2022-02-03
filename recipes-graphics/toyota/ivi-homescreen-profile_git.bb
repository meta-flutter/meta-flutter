require ivi-homescreen.inc

FLUTTER_RUNTIME = "profile"

RDEPENDS_${PN} += "flutter-engine-${FLUTTER_RUNTIME}"

SERVICE_EXEC_START_PARAMS = "--start-paused"
