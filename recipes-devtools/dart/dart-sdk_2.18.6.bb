#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Dart SDK"
DESCRIPTION = "The Dart SDK, including the VM, dart2js, core libraries, and more."
AUTHOR = "Dart Team"
HOMEPAGE = "https://github.com/dart-lang/sdk"
BUGTRACKER = "https://github.com/dart-lang/sdk/issues"
SECTION = "devtools"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=29b4ad63b1f1509efea6629404336393"


DEPENDS += "\
    compiler-rt \
    libcxx \
    "

SRCREV = "f16b62ea92cc0f04cfd9166992f93419e425c809"
SRC_URI = "gn://github.com/dart-lang/sdk.git;name=sdk \
           file://0001-External-Toolchain.patch"

S = "${WORKDIR}/sdk"

inherit gn-for-flutter python3native pkgconfig

require conf/include/gn-utils.inc

# For gn.bbclass
EXTRA_GN_SYNC ?= "--shallow --no-history -R -D"

# Toolchain setup
RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"

PACKAGECONFIG ??= "platform-sdk verify-sdk-hash mallinfo2"

PACKAGECONFIG[verify-sdk-hash] = "--verify-sdk-hash, --no-verify-sdk-hash"
PACKAGECONFIG[mallinfo2] = "--use-mallinfo2"
PACKAGECONFIG[platform-sdk] = "--platform-sdk"
PACKAGECONFIG[use-crashpad] = "--use-crashpad"
PACKAGECONFIG[use-qemu] = "--use-qemu"
PACKAGECONFIG[exclude-kernel-service] = "--exclude-kernel-service"
PACKAGECONFIG[verbose] = "--verbose"

GN_ARGS = "${PACKAGECONFIG_CONFARGS} --no-goma --clang"

# debug, release, product
GN_ARGS:append = " --mode product"

GN_ARGS:append:armv7 = " --arch arm --arm-float-abi ${TARGET_FPU}"
GN_ARGS:append:armv7a = " --arch arm --arm-float-abi ${TARGET_FPU}"
GN_ARGS:append:armv7ve = " --arch arm --arm-float-abi ${TARGET_FPU}"
GN_ARGS:append:aarch64 = " --arch arm64"
GN_ARGS:append:x86-64 = " --arch x64"
GN_ARGS:append:riscv32 = " --arch riscv32"
GN_ARGS:append:riscv64 = " --arch riscv64"


OUT_DIR = "${S}/out"

do_compile() {
    cd ${S}

    # we only build one mode type
    rm -rf "${OUT_DIR}" || true

    export DART_USE_SYSROOT="${TARGET_SYSROOT}"
    export DART_USE_TOOLCHAIN="${STAGING_DIR_NATIVE}/usr/bin"

    #
    # additional flags that may be useful:
    #
    # --debug-opt-level
    # --gn-args

    bbnote "GN_ARGS: ${GN_ARGS}"
    ${PYTHON} ./tools/gn.py ${GN_ARGS}

    BUILD_DIR="${OUT_DIR}/$(ls ${OUT_DIR})"

    bbnote "$(cat "${BUILD_DIR}/args.gn")"

    autoninja -C "${BUILD_DIR}" create_sdk
}
do_compile[depends] += "depot-tools-native:do_populate_sysroot"
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    BUILD_DIR="${OUT_DIR}/$(ls ${OUT_DIR})"

    install -d ${D}${datadir}/${BPN}
    cp -R ${BUILD_DIR}/${BPN}/* ${D}${datadir}/${BPN}/

    # enable auto dependency detection and executable stripping
    install -m 0775 ${BUILD_DIR}/dart \
        ${D}${datadir}/${BPN}/bin/dart

    install -m 0775 ${BUILD_DIR}/gen_snapshot_product \
        ${D}${datadir}/${BPN}/bin/utils/gen_snapshot

    install -m 0775 ${BUILD_DIR}/dart_precompiled_runtime_product \
        ${D}${datadir}/${BPN}/bin/dartaotruntime
}

FILES:${PN} += "${datadir}"

BBCLASSEXTEND = "native nativesdk"
