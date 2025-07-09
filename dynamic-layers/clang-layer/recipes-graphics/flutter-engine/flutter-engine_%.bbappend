#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

DEPENDS:riscv64 += "\
    compiler-rt \
    libcxx \
    "

RUNTIME:riscv64 = "llvm"
TOOLCHAIN:riscv64 = "clang"
PREFERRED_PROVIDER_libgcc:riscv64 = "compiler-rt"
LIBCPLUSPLUS:riscv64 = "-stdlib=libc++"

#
# RISC-V specific
#

COMPATIBLE_MACHINE:riscv64 = "(.*)"

FILESEXTRAPATHS:prepend := "${THISDIR}/files/:"

SRC_URI_EXTRA:riscv64 += "\
    file://0001-gn-riscv32-and-riscv64.patch \
    file://0002-fml-build-config-add-riscv.patch \
    file://0003-swiftshader-riscv-support.patch \
    file://0004-tonic-riscv-support.patch \
    file://0005-abseil-clang-compiler-warnings.patch \
    file://0006-Add-risc-v-32-64-support-to-native-assets.patch \
    "

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