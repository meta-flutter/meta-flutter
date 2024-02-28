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
    rive-taffy-ffi \
"

# Toolchain setup
RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"
LIBCPLUSPLUS = "-stdlib=libc++"

SRC_URI = " \
    https://pub.dartlang.org/packages/rive_common/versions/0.3.0.tar.gz;downloadfilename=pub-dartlang-rive_common-0.3.0.tar.gz;subdir=src;name=common \
    git://github.com/harfbuzz/harfbuzz.git;protocol=https;branch=main;destsuffix=src/third_party/harfbuzz;name=harfbuzz \
    git://github.com/Tehreer/SheenBidi.git;protocol=https;branch=master;destsuffix=src/third_party/sheenbidi;name=sheenbidi \
    git://github.com/rive-app/miniaudio.git;protocol=https;branch=rive;destsuffix=src/third_party/miniaudio;name=miniaudio \
    file://CMakeLists.txt;subdir=src \
"

SRC_URI[common.sha256sum] = "a6d05f65985e3ec18b7051ced6316d56374e0ca9c58288c37dd8510cb077832b"

SRCREV_FORMAT .= "harfbuzz"
SRCREV_harfbuzz = "afcae83a064843d71d47624bc162e121cc56c08b"
SRCREV_FORMAT .= "sheenbidi"
SRCREV_sheenbidi = "adfccc46504b1be37b8894f064121c56c31312f7"
SRCREV_FORMAT .= "miniaudio"
SRCREV_miniaudio = "4b8300ad2249fe292ad7eef492abf78d9c8462eb"

S = "${WORKDIR}/src"

inherit cmake

EXTRA_OECMAKE += " \
    -D TAFFY_FFI_LINK_LIBRARY=${STAGING_LIBDIR}/taffy_ffi/libtaffy_ffi.a \
"

FILES:${PN}-dev = "${libdir}"

