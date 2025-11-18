#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "rive_common"
DESCRIPTION = "Rive Text Shared Module"
AUTHOR = "Rive"
HOMEPAGE = "https://github.com/rive-app/rive-flutter"
BUGTRACKER = "https://github.com/rive-app/rive-flutter"
SECTION = "devtools"

LICENSE = "MIT & Apache-2.0"
LIC_FILES_CHKSUM = " \
    file://LICENSE;md5=c52243a14a066c83e50525d9ad046678 \
    file://macos/rive-cpp/LICENSE;md5=9a8a5e004196b5614e34130e5558fb6c \
    file://macos/harfbuzz/COPYING;md5=b98429b8e8e3c2a67cfef01e99e4893d \
    file://macos/SheenBidi/LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57 \
    file://macos/yoga/LICENSE;md5=901f6cd9846257b3a9c69dbd0a49caf1 \
"

DEPENDS += "\
    compiler-rt \
    libcxx \
"

SRCREV = "4e7699cb628276ec118eac1150cf7d1b34c18c14"
SRC_URI = "git://github.com/meta-flutter/rive-common.git;protocol=https;lfs=0;nobranch=1"

TOOLCHAIN = "clang"
TOOLCHAIN_NATIVE = "clang"
TC_CXX_RUNTIME = "llvm"
PREFERRED_PROVIDER_llvm = "clang"
PREFERRED_PROVIDER_llvm-native = "clang-native"
PREFERRED_PROVIDER_libgcc = "compiler-rt"
LIBCPLUSPLUS = "-stdlib=libc++"

inherit cmake

FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "dev-so"
FILES:${PN} = "${libdir}"
