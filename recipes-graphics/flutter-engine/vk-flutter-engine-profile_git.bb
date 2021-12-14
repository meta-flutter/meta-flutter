require flutter-engine.inc

FLUTTER_RUNTIME = "profile"

FLUTTER_CLANG_VERSION = "14.0.0"

FLUTTER_ENGINE_REPO_URL = "https://github.com/bdero/flutter-engine.git@bdero/vulkan-embedder"

do_patch:append () {

    # GCC 11 specific patches

    cd ${S}

    cd third_party/dart
    git apply ${WORKDIR}/0001-dart-enable-mallinfo2.patch
    cd ../..

    cd third_party/swiftshader
    git apply ${WORKDIR}/0001-swiftshader-enable-mallinfo2.patch
    cd ../..
}

RPROVIDES:${PN} = "vk-flutter-engine-${FLUTTER_RUNTIME}"