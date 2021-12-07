include flutter-pi.inc

FLUTTER_RUNTIME = "release"

DEPENDS += "flutter-engine-${FLUTTER_RUNTIME}"
RDEPENDS_${PN} += "flutter-engine-${FLUTTER_RUNTIME}"
