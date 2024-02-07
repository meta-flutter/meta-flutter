#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "PDFium"
DESCRIPTION = "PDF rendering engine"
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
    zlib \
    "

SRCREV = "7c7a6087e09e1a344984a6d0c5fbc2af36eca7ea"
SRC_URI = "\
    gn://pdfium.googlesource.com/pdfium.git;gn_name=pdfium \
    file://public_headers.patch \
    file://shared_library.patch \
    file://v8_init.patch \
    file://0001-arm64-gcc-tool-prefix.patch \
    file://0001-linker-flags.patch \
    "

S = "${WORKDIR}/pdfium"
B = "${WORKDIR}/pdfium/out"

inherit gn-fetcher pkgconfig

require conf/include/gn-utils.inc

# For gn.bbclass
GN_CUSTOM_VARS ?= '{"checkout_configuration": "small"}'
EXTRA_GN_SYNC ?= "--shallow --no-history -R -D"

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE:aarch64 = "(.*)"
COMPATIBLE_MACHINE:x86-64 = "(.*)"


PACKAGECONFIG ??= "release v8"

PACKAGECONFIG[release] = "is_debug=false, is_debug=true"

PACKAGECONFIG[v8] = "pdf_enable_v8=true pdf_enable_xfa=true, pdf_enable_v8=false pdf_enable_xfa=false"

GN_TARGET_ARCH_NAME:aarch64 = "arm64"
GN_TARGET_ARCH_NAME:arm = "arm"
GN_TARGET_ARCH_NAME:x86-64 = "x64"

GN_ARGS = '\
    ${PACKAGECONFIG_CONFARGS} \
    pdf_is_standalone = true \
    pdf_use_partition_alloc = false \
    is_component_build = false \
    use_allocator_shim = false \
    treat_warnings_as_errors = false \
    use_system_freetype = true \
    use_system_libopenjpeg2 = false \
    use_system_zlib = true \
    use_system_libpng = true \
    \
    is_clang = false \
    use_custom_libcxx = false \
    libcxx_is_shared = false \
    \
    target_os = "linux" \
    target_cpu = "${GN_TARGET_ARCH_NAME}" \
    target_triple = "${TARGET_SYS}" \
    target_sysroot = "${STAGING_DIR_TARGET}" \
'

do_configure() {
    cd ${S}
    gn gen --args='${GN_ARGS}' "${B}"
}

do_compile() {
    ninja -C "${B}" pdfium $PARALLEL_MAKE
}
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {
    install -d ${D}${libdir}/pdfium
    install -m 0755 ${B}/libpdfium.so ${D}${libdir}/pdfium
    install -m 0644 ${B}/icudtl.dat ${D}${libdir}/pdfium
    cp ${S}/LICENSE ${D}${libdir}/pdfium
    cp ${B}/args.gn ${D}${libdir}/pdfium

    if ${@bb.utils.contains('PACKAGECONFIG', 'v8', 'true', 'false', d)}; then
        install -m 0644 ${B}/snapshot_blob.bin ${D}${libdir}/pdfium/snapshot_blob.bin
    fi

    install -d ${D}${includedir}
    cp -R ${S}/public/* ${D}${includedir}
}

FILES:${PN}-dev += "\
    ${libdir}/pdfium/LICENSE \
    ${libdir}/pdfium/args.gn \
    ${includedir}/PRESUBMIT.py \
"

SOLIBS = ".so"
FILES_SOLIBSDEV = ""
