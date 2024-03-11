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
    file://third_party/harfbuzz/COPYING;md5=6ee0f16281694fb6aa689cca1e0fb3da \
    file://third_party/sheenbidi/LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57 \
    file://third_party/miniaudio/LICENSE;md5=a8b3ebda41490db74d01d85490f12b88 \
"

DEPENDS += "\
    compiler-rt \
    libcxx \
"

# Toolchain setup
RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"
LIBCPLUSPLUS = "-stdlib=libc++"

SRCREV = "08cdd32c85b777b8ec6f8643cfab41446b180426"
SRC_URI = "gitsm://github.com/meta-flutter/rive-common.git;protocol=https;lfs=0;nobranch=1"

S = "${WORKDIR}/git"

inherit cmake

FILES:${PN} = "${libdir}"
