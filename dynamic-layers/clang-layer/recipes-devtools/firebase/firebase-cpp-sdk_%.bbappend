
DEPENDS += "\
    compiler-rt \
    libcxx \
    "

RUNTIME = "llvm"
TOOLCHAIN = "clang"
TOOLCHAIN:class-native = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"
