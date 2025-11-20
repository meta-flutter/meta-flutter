#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

DEPENDS:riscv64 += "\
    compiler-rt \
    libcxx \
    "

RUNTIME = "llvm"
TOOLCHAIN = "clang"
LIBCPLUSPLUS = "-stdlib=libc++"

#
# RISC-V specific
#

COMPATIBLE_MACHINE:riscv32 = "(.*)"
COMPATIBLE_MACHINE:riscv64 = "(.*)"


# Use Yocto clang for riscv64; required for linking
CLANG_PATH:riscv64 = "${STAGING_DIR_NATIVE}/usr"

do_configure:append() {
    cd ${STAGING_DIR_TARGET}/usr/lib

    test -e crtbeginS.o && rm crtbeginS.o
    test -e crtendS.o && rm crtendS.o
    test -e libgcc.a && rm libgcc.a

    ln -s "$(find -iname crtbeginS.o)" crtbeginS.o
    ln -s "$(find -iname crtendS.o)" crtendS.o
    ln -s "$(find -iname libgcc.a)" libgcc.a
}