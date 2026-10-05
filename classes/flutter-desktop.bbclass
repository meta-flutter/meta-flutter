require conf/include/flutter-app.inc

DEPENDS += " \
    cmake-native \
    compiler-rt \
    libcxx \
    ninja-native \
    "

RUNTIME = "llvm"
require conf/include/flutter-clang-toolchain.inc
PREFERRED_PROVIDER_libgcc = "compiler-rt"
