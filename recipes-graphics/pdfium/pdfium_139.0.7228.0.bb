#
# Copyright (c) 2020-2025 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "PDFium"
DESCRIPTION = "PDF rendering engine \
               Reference: https://github.com/bblanchon/pdfium-binaries"
AUTHOR = "Google PDFium Team"
HOMEPAGE = "https://pdfium.googlesource.com/pdfium"
BUGTRACKER = "https://bugs.chromium.org/p/pdfium/issues/list"
SECTION = "graphics"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c93507531cc9bb8e24a05f2a1a4036c7"

DEPENDS += "\
    freetype \
    glib-2.0 \
    libpng \
    openjpeg \
    zlib \
    depot-tools-native \
    ninja-native \
    "

DEPENDS:append:x86-64 = " nasm-native"

SRCREV = "4e4d7a14a4d9d484feb4a4770a892cd964cfd968"
SRC_URI = "\
    gn://pdfium.googlesource.com/pdfium.git \
    \
    file://0001-public-headers.patch \
    file://0002-shared-library.patch \
    file://0003-sysroot-fix-path.patch \
    file://0004-v8-init.patch \
    \
    file://toolchain.gn.in \
    "

S = "${UNPACKDIR}/gn/pdfium"
B = "${UNPACKDIR}/gn/out"

inherit gn-fetcher pkgconfig

require conf/include/gn-utils.inc

# For gn.bbclass
# Trick `gclient sync`. Download some binary in order to satisfy dependencies.
# Check reasoning at:
# - https://github.com/meta-flutter/meta-flutter/issues/411#issuecomment-2955621158
# Keep this workaround while the following issue is unsolved:
# - https://issues.chromium.org/issues/424205782
GN_CUSTOM_VARS ?= '\
{\
    "reclient_package": "gn/gn/", \
    "reclient_version": "git_revision:b99a82ca8ee957da829d6313b818b99df8e7ccb8", \
    "checkout_configuration": "small" \
}'

EXTRA_GN_SYNC ?= "--shallow --no-history -R -D"

# cc here is a bare ${TARGET_SYS}-gcc, so nothing the distro puts on CC reaches
# the compile lines gn bakes into build.ninja. TARGET_CC_ARCH carries the tune,
# security and 64-bit time flags -- oe-core's time64.inc appends
# -D_TIME_BITS=64 -D_FILE_OFFSET_BITS=64 there for 32-bit targets -- and
# without them libpdfium.so trips the 32bit-time QA check on arm. See #977.
PDFIUM_TOOLCHAIN_FLAGS = "${TARGET_CC_ARCH}"

EXTRA_CFLAGS = "${PDFIUM_TOOLCHAIN_FLAGS}"
EXTRA_ASMFLAGS = "${PDFIUM_TOOLCHAIN_FLAGS}"

# Substituted into extra_cxxflags. Before #977 this held a whole gn assignment
# and was pasted in bare, so the musl value below emitted a stray flag as a
# statement and gn could not parse the toolchain file.
EXTRA_CXXFLAGS = "${PDFIUM_TOOLCHAIN_FLAGS}"
EXTRA_CXXFLAGS:append:libc-musl = " -flax-vector-conversions"

# current_cpu for the native toolchain in toolchain.gn.in.
GN_HOST_ARCH = "${@gn_host_arch_name(d)}"

PACKAGECONFIG ??= "release"

PACKAGECONFIG[release] = "is_debug = false, is_debug = true"
PACKAGECONFIG[skia] = "pdf_use_skia = true, pdf_use_skia = false, fontconfig"
PACKAGECONFIG[v8] = "pdf_enable_v8=true pdf_enable_xfa=true, pdf_enable_v8=false pdf_enable_xfa=false"

GN_ARGS = '\
    ${PACKAGECONFIG_CONFARGS} \
    pdf_is_standalone = true \
    is_component_build = false \
    treat_warnings_as_errors = false \
    \
    use_system_freetype = true \
    use_system_libopenjpeg2 = true \
    use_system_zlib = true \
    use_system_libpng = true \
    \
    is_clang = false \
    clang_use_chrome_plugins = false \
    use_custom_libcxx = false \
    libcxx_is_shared = false \
    \
    target_os = "linux" \
    target_cpu = "${GN_TARGET_ARCH_NAME}" \
    target_triple = "${TARGET_SYS}" \
    target_sysroot = "${STAGING_DIR_TARGET}" \
    \
    host_toolchain = "//build/toolchain/linux:native" \
'

do_configure() {
    cd ${S}

    #
    # configure toolchain file
    #
    cp ${S}/../../toolchain.gn.in ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@GN_TARGET_ARCH_NAME@|${GN_TARGET_ARCH_NAME}|g" ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@TARGET_SYS@|${TARGET_SYS}|g"                   ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@LDFLAGS@|${LDFLAGS}|g"                         ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@EXTRA_CFLAGS@|${EXTRA_CFLAGS}|g"               ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@EXTRA_CXXFLAGS@|${EXTRA_CXXFLAGS}|g"           ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@EXTRA_ASMFLAGS@|${EXTRA_ASMFLAGS}|g"           ${S}/build/toolchain/linux/BUILD.gn

    # native toolchain
    sed -i "s|@GN_HOST_ARCH@|${GN_HOST_ARCH}|g"               ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@BUILD_PREFIX@|${BUILD_PREFIX}|g"               ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@BUILD_CC@|${BUILD_CC}|g"                       ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@BUILD_CXX@|${BUILD_CXX}|g"                     ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@BUILD_AR@|${BUILD_AR}|g"                       ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@BUILD_NM@|${BUILD_NM}|g"                       ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@BUILD_CFLAGS@|${BUILD_CFLAGS}|g"               ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@BUILD_CXXFLAGS@|${BUILD_CXXFLAGS}|g"           ${S}/build/toolchain/linux/BUILD.gn
    sed -i "s|@BUILD_LDFLAGS@|${BUILD_LDFLAGS}|g"             ${S}/build/toolchain/linux/BUILD.gn

    gn gen --args='${GN_ARGS}' "${B}"
}

do_compile() {
    ninja -C "${B}" pdfium $PARALLEL_MAKE
}
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {
    install -d ${D}${libdir}/pdfium
    install -m 0755 ${B}/libpdfium.so ${D}${libdir}/pdfium
    cp ${S}/LICENSE ${D}${libdir}/pdfium

    if ${@bb.utils.contains('PACKAGECONFIG', 'v8', 'true', 'false', d)}; then
        # Only V8 loads ICU data. Core pdfium uses ICU character
        # properties, which are built into libicuuc. See #991.
        install -m 0644 ${B}/icudtl.dat ${D}${libdir}/pdfium
        install -m 0644 ${B}/snapshot_blob.bin ${D}${libdir}/pdfium/snapshot_blob.bin
    fi

    install -d ${D}${includedir}
    cp -R ${S}/public/* ${D}${includedir}
}

FILES:${PN}-dev += "\
    ${libdir}/pdfium/LICENSE \
    ${includedir}/PRESUBMIT.py \
"