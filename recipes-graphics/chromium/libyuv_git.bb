DESCRIPTION = "libyuv is an open source project that includes YUV scaling and conversion functionality."
SUMMARY = "libyuv is an open source project that includes YUV scaling and conversion functionality."
AUTHOR = "Google"
HOMEPAGE = "https://chromium.googlesource.com/libyuv/libyuv/"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=464282cfb405b005b9637f11103a7325"

DEPENDS += "\
    compiler-rt \
    libcxx \
    libjpeg-turbo \
"

PV = "1.0+git${SRCPV}"
SRCREV = "a04e4f87fbf40405707b1d0ae9fcba8fc93f7856"
SRC_URI = "git://chromium.googlesource.com/libyuv/libyuv.git;protocol=https;branch=main"

S = "${WORKDIR}/git"

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER:libgcc = "compiler-rt"

inherit cmake

EXTRA_OECMAKE = "-D TEST=OFF"
