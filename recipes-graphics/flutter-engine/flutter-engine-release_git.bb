require flutter-engine.inc

require conf/include/flutter-engine_${FLUTTER_SDK_TAG}.inc

FLUTTER_RUNTIME = "release"

RPROVIDES_${PN} = "flutter-engine-${FLUTTER_RUNTIME}"
