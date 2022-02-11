include flutter-pi.inc

FLUTTER_RUNTIME = "profile"

DEPENDS += "flutter-engine-${FLUTTER_RUNTIME}"
RDEPENDS:${PN} += "flutter-engine-${FLUTTER_RUNTIME}"
