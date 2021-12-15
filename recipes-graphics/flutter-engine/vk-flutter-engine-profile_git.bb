require flutter-engine.inc

FLUTTER_RUNTIME = "profile"

FLUTTER_ENGINE_REPO_URL = "https://github.com/bdero/flutter-engine.git@bdero/vulkan-embedder"

GCLIENT_ARGS_EXTRA = " "

RPROVIDES_${PN} = "vk-flutter-engine-${FLUTTER_RUNTIME}"
