SUMMARY = "PDFium"
DESCRIPTION = "PDF rendering engine"
AUTHOR = "Google PDFium Team"
HOMEPAGE = "https://pdfium.googlesource.com/pdfium"
BUGTRACKER = "https://bugs.chromium.org/p/pdfium/issues/list"
SECTION = "graphics"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c93507531cc9bb8e24a05f2a1a4036c7"

DEPENDS += "\
    compiler-rt \
    compiler-rt-native \
    glib-2.0 \
    libcxx \
    libcxx-native \
    "

SRCREV = "ea231736ea4056dcef435caee59580701aa4b3ef"
SRC_URI = "gn://pdfium.googlesource.com/pdfium.git \
           file://public_headers.patch \
           file://shared_library.patch \
           file://v8_init.patch \
           "

S = "${WORKDIR}/src"
B = "${WORKDIR}/src/out"

inherit gn-for-flutter python3native pkgconfig

require conf/include/gn-utils.inc

RUNTIME = "llvm"
TOOLCHAIN = "clang"
TC_CXX_RUNTIME = "llvm"
TCLIBC = "glibc"
PREFERRED_PROVIDER:libgcc = "compiler-rt"

# For gn.bbclass
EXTRA_GN_SYNC ?= "--shallow --no-history -R -D"

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE:aarch64 = "(.*)"
COMPATIBLE_MACHINE:armv7 = "(.*)"
COMPATIBLE_MACHINE:armv7a = "(.*)"
COMPATIBLE_MACHINE:armv7ve = "(.*)"
COMPATIBLE_MACHINE:x86 = "(.*)"
COMPATIBLE_MACHINE:x86-64 = "(.*)"

PACKAGECONFIG ??= "release v8"

PACKAGECONFIG[release] = "\
    is_debug=false, \
    is_debug=true \
"

PACKAGECONFIG[v8] = "\
    pdf_enable_v8=true pdf_enable_xfa=true, \
    pdf_enable_v8=false pdf_enable_xfa=false \
"

PACKAGECONFIG[musl] = "\
"

GN_TARGET_ARCH_NAME:aarch64 = "arm64"
GN_TARGET_ARCH_NAME:arm = "arm"
GN_TARGET_ARCH_NAME:x86 = "x86"
GN_TARGET_ARCH_NAME:x86-64 = "x64"

GN_ARGS = '\
    ${PACKAGECONFIG_CONFARGS} \
    pdf_is_standalone = true \
    is_component_build = false \
    treat_warnings_as_errors = false \
    sysroot = "${STAGING_DIR_NATIVE}" \
    target_os = "linux" \
    target_cpu = "${GN_TARGET_ARCH_NAME}" \
    target_triple = "${TARGET_SYS}" \
    target_sysroot = "${STAGING_DIR_TARGET}" \
    arm_tune = "${@gn_get_tune_features(d)}" \
    arm_float_abi = "${TARGET_FPU}" \
'

def gn_get_tune_features(d):
    """ Returns tune value """
    tune_features = d.getVar("TUNE_FEATURES")
    if not tune_features:
        return tune_features
    #tune_features = tune_features.replace("aarch64 ", "")
    #tune_features = tune_features.replace("arm ", "")
    #tune_features = tune_features.replace("vfp ", "")
    return tune_features.replace(" ", "+")

def gn_clang_triple_prefix(d):
    """Returns compiler triple prefix corresponding to the target's machine
    architecture."""
    import re
    arch_translations = {
        r'arm64.*': 'aarch64-unknown-linux-gnu',
        r'arm.*': 'arm-unknown-linux-gnueabihf',
        r'x86$': 'i386-unknown-linux-gnu',
        r'x64$': 'x86_64-unknown-linux-gnu',
    }
    build_arch = d.getVar("GN_TARGET_ARCH_NAME")
    for arch_regexp, gn_arch_name in arch_translations.items():
        if re.match(arch_regexp, build_arch):
            return gn_arch_name

    bb.fatal('Unsuported GN_TARGET_ARCH_NAME value: "%s"' % build_arch)

CLANG_TOOLCHAIN_TRIPLE = "${@gn_clang_triple_prefix(d)}"
CLANG_PATH = "${S}/third_party/llvm-build/Release+Asserts"

do_configure() {
    # copy GCC files to location pdfium Clang uses
    CLANG_VERSION=`ls ${CLANG_PATH}/lib/clang`
    GCC_VERSION=`ls ${STAGING_LIBDIR}/${TARGET_SYS}`
    CLANG_LIB_TARGET_PATH=${CLANG_PATH}/lib/clang/${CLANG_VERSION}/lib/${CLANG_TOOLCHAIN_TRIPLE}
    mkdir -p ${CLANG_LIB_TARGET_PATH}
    cp -r ${STAGING_LIBDIR}/${TARGET_SYS}/${GCC_VERSION}/* ${CLANG_LIB_TARGET_PATH}/

    gn gen --args='${GN_ARGS}' "${B}"
}

do_compile() {
    autoninja -C "${B}" pdfium
}
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {
    install -d ${D}${libdir}/pdfium
    install -m 0755 ${B}/libpdfium.so ${D}${libdir}
    install -m 0644 ${B}/icudtl.dat ${D}${libdir}/pdfium
    install -m 0644 ${B}/snapshot_blob.bin ${D}${libdir}/pdfium
    cp ${S}/LICENSE ${D}${libdir}/pdfium
    cp ${B}/args.gn ${D}${libdir}/pdfium
    cp ${B}/VERSION ${D}${libdir}/pdfium

    install -d ${D}${includedir}
    cp -R ${S}/public/* ${D}${includedir}
    rm -f ${D}${includedir}/DEPS
    rm -f ${D}${includedir}/README
    rm -f ${D}${includedir}/PRESUBMIT.py
}

SOLIBS = ".so"
FILES_SOLIBSDEV = ""
