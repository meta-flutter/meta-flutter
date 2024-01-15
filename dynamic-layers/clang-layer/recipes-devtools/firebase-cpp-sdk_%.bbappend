
DEPENDS += "\
    compiler-rt \
    libcxx \
    ninja-native \
    "

RUNTIME = "llvm"
TOOLCHAIN = "clang"
TOOLCHAIN:class-native = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"
