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
LIC_FILES_CHKSUM = "file://LICENSE;md5=c52243a14a066c83e50525d9ad046678"

DEPENDS += "\
    compiler-rt \
    libcxx \
"

SRCREV = "4e7699cb628276ec118eac1150cf7d1b34c18c14"
SRC_URI = "git://github.com/meta-flutter/rive-common.git;protocol=https;lfs=0;nobranch=1"

inherit cmake

# Toolchain setup
RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"
LIBCPLUSPLUS = "-stdlib=libc++"

FILES_SOLIBSDEV = ""
INSANE_SKIP:${PN} += "dev-so"
FILES:${PN} = "${libdir}"
