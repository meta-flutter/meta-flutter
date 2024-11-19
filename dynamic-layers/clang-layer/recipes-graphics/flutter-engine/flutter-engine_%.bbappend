
DEPENDS += "\
    compiler-rt \
    libcxx \
    "

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"
LIBCPLUSPLUS = "-stdlib=libc++"

CLANG_PATH = "${STAGING_DIR_NATIVE}/usr"

do_configure:append() {
    cwd=$(pwd)
    cd ${STAGING_DIR_TARGET}/usr/lib

    test -e crtbeginS.o && rm crtbeginS.o
    test -e crtendS.o && rm crtendS.o
    test -e libgcc.a && rm libgcc.a

    ln -s "$(find -iname crtbeginS.o)" crtbeginS.o
    ln -s "$(find -iname crtendS.o)" crtendS.o
    ln -s "$(find -iname libgcc.a)" libgcc.a

    cd $cwd
}

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
    "
