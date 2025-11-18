
DEPENDS += "\
    compiler-rt \
    libcxx \
    "

# Toolchain setup
RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"
LIBCPLUSPLUS = "-stdlib=libc++"

EXTRA_OECMAKE += "\
    -D LLVM_CONFIG=${STAGING_BINDIR_NATIVE}/llvm-config \
    "
