#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

# The engine links with lld and builds against libc++. On master both come from
# openembedded-core, which now carries clang; dunfell long predates that, so
# they are meta-clang's to provide and belong here rather than in the recipe --
# a dependency the recipe cannot satisfy without this layer stops it parsing.
#
# meta-clang has no separate lld recipe. lld is built inside clang, so ld.lld
# arrives with clang-native rather than an lld-native of its own:
#
#   ERROR: Nothing PROVIDES 'lld-native' ... Close matches: llvm-native
DEPENDS += "\
    libcxx \
    clang-native \
    "

DEPENDS_riscv64 += "\
    compiler-rt \
    "

RUNTIME = "llvm"
TOOLCHAIN = "clang"
LIBCPLUSPLUS = "-stdlib=libc++"

#
# RISC-V specific
#

COMPATIBLE_MACHINE_riscv32 = "(.*)"
COMPATIBLE_MACHINE_riscv64 = "(.*)"


do_configure_append() {
    cd ${STAGING_DIR_TARGET}/usr/lib

    test -e crtbeginS.o && rm crtbeginS.o
    test -e crtendS.o && rm crtendS.o
    test -e libgcc.a && rm libgcc.a

    ln -s "$(find -iname crtbeginS.o)" crtbeginS.o
    ln -s "$(find -iname crtendS.o)" crtendS.o
    ln -s "$(find -iname libgcc.a)" libgcc.a
}