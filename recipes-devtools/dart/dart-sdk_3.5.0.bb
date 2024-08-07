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
    curl-native \
    depot-tools-native \
    ninja-native \
    xz-native \
    "

SRCREV = "a97f632612f2889d42721afb5e0a7e89ade47237"
SRC_URI = " \
    gn://github.com/dart-lang/sdk.git;gn_name=sdk \
    file://gcc_toolchain.gni.in \
"

S = "${WORKDIR}/sdk"

inherit gn-fetcher pkgconfig

require conf/include/gn-utils.inc

# For gn.bbclass
EXTRA_GN_SYNC ?= "--shallow --no-history -R -D"


PACKAGECONFIG ??= "platform-sdk verify-sdk-hash mallinfo2"

PACKAGECONFIG[verify-sdk-hash] = "--verify-sdk-hash, --no-verify-sdk-hash"
PACKAGECONFIG[mallinfo2] = "--use-mallinfo2"
PACKAGECONFIG[platform-sdk] = "--platform-sdk"
PACKAGECONFIG[use-crashpad] = "--use-crashpad"
PACKAGECONFIG[use-qemu] = "--use-qemu"
PACKAGECONFIG[exclude-kernel-service] = "--exclude-kernel-service"
PACKAGECONFIG[verbose] = "--verbose"

GN_ARGS = "${PACKAGECONFIG_CONFARGS} --no-rbe"

# debug, release, product
GN_ARGS:append = " --mode product"

GN_HOST_ARCH = "${@gn_host_arch_name(d)}"

GN_ARGS:append:armv7 = " --arch arm_${GN_HOST_ARCH} --arm-float-abi ${TARGET_FPU}"
GN_ARGS:append:armv7a = " --arch arm_${GN_HOST_ARCH} --arm-float-abi ${TARGET_FPU}"
GN_ARGS:append:armv7ve = " --arch arm_${GN_HOST_ARCH} --arm-float-abi ${TARGET_FPU}"
GN_ARGS:append:aarch64 = " --arch arm64_${GN_HOST_ARCH}"
GN_ARGS:append:x86-64 = " --arch x64_${GN_HOST_ARCH}"
GN_ARGS:append:riscv32 = " --arch riscv32_${GN_HOST_ARCH}"
GN_ARGS:append:riscv64 = " --arch riscv64_${GN_HOST_ARCH}"

OUT_DIR = "${S}/out"

do_configure() {
    cd ${S}

    # prevent tmp path warning
    cp ${WORKDIR}/gcc_toolchain.gni.in ${S}/build/toolchain/gcc_toolchain.gni
    sed -i "s|@DEBUG_FLAGS@|${DEBUG_FLAGS}|g" ${S}/build/toolchain/gcc_toolchain.gni

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
}
do_configure[network] = "1"
do_configure[depends] += " \
    depot-tools-native:do_populate_sysroot \
    "

do_compile() {
    cd ${S}
    export DART_USE_SYSROOT="${TARGET_SYSROOT}"
    export DART_USE_TOOLCHAIN="${STAGING_DIR_NATIVE}/usr/bin"

    BUILD_DIR="${OUT_DIR}/$(ls ${OUT_DIR})"

    bbnote "$(cat "${BUILD_DIR}/args.gn")"

    ninja -C "${BUILD_DIR}" create_sdk $PARALLEL_MAKE
}
do_compile[depends] += " \
    depot-tools-native:do_populate_sysroot \
    "
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {

    BUILD_DIR="${OUT_DIR}/$(ls ${OUT_DIR})"

    install -d ${D}${datadir}/dart-sdk

    cp -R ${BUILD_DIR}/dart-sdk/* ${D}${datadir}/dart-sdk/
}

INSANE_SKIP:${PN} = "already-stripped ldflags"

FILES:${PN} += "${datadir}"

BBCLASSEXTEND = "native nativesdk"
