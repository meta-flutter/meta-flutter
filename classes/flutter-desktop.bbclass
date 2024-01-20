require conf/include/flutter-app.inc

DEPENDS += " \
    cmake-native \
    compiler-rt \
    libcxx \
    ninja-native \
    "

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"
