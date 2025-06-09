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
LIC_FILES_CHKSUM = "file://sdk/LICENSE;md5=29b4ad63b1f1509efea6629404336393"


DEPENDS += "\
    curl-native \
    depot-tools-native \
    ninja-native \
    xz-native \
    "

S = "${WORKDIR}/gn"

SRCREV = "05589740efb305ceef593b3db6cab2910c17d480"
SRC_URI = " \
    gn://github.com/dart-lang/sdk.git;gn_name=sdk \
    file://gcc_toolchain.gni.in \
"

inherit gn-fetcher pkgconfig

require conf/include/gn-utils.inc

# For gn.bbclass
EXTRA_GN_SYNC ?= "--shallow --no-history -R -D"


PACKAGECONFIG ??= "platform-sdk verify-sdk-hash mallinfo2 dynamic-modules"

PACKAGECONFIG[verify-sdk-hash] = "--verify-sdk-hash"
PACKAGECONFIG[mallinfo2] = "--use-mallinfo2"
PACKAGECONFIG[platform-sdk] = "--platform-sdk"
PACKAGECONFIG[use-crashpad] = "--use-crashpad"
PACKAGECONFIG[use-qemu] = "--use-qemu"
PACKAGECONFIG[exclude-kernel-service] = "--exclude-kernel-service"
PACKAGECONFIG[clang] = "--clang, --no-clang"
PACKAGECONFIG[verbose] = "--verbose"
PACKAGECONFIG[git-version] = "--git-version"
PACKAGECONFIG[dynamic-modules] = "--dart-dynamic-modules"
PACKAGECONFIG[codesigning-identity] = "--codesigning-identity ${CODESIGNING_IDENTITY}"

GN_ARGS = "${PACKAGECONFIG_CONFARGS} --no-rbe"

# all, debug, release, product
GN_ARGS:append = " --mode product"

GN_HOST_ARCH = "${@gn_host_arch_name(d)}"

# available architectures
# all,ia32,x64,
# arm,arm64,arm_x64,arm_arm64,
# simarm,simarm64,simarm_x64,simarm_arm64,x64c,arm64c,simarm64c,
# simriscv32,simriscv64,simx64,simx64c,riscv32,riscv64

# --arm-float-abi [soft,softfp,hard]

GN_ARGS:append:armv7 = " -a arm_${GN_HOST_ARCH} --arm-float-abi ${TARGET_FPU}"
GN_ARGS:append:armv7a = " -a arm_${GN_HOST_ARCH} --arm-float-abi ${TARGET_FPU}"
GN_ARGS:append:armv7ve = " -a arm_${GN_HOST_ARCH} --arm-float-abi ${TARGET_FPU}"
GN_ARGS:append:aarch64 = " -a arm64_${GN_HOST_ARCH}"
GN_ARGS:append:x86-64 = " -a x64_${GN_HOST_ARCH}"
GN_ARGS:append:riscv32 = " -a riscv32_${GN_HOST_ARCH}"
GN_ARGS:append:riscv64 = " -a riscv64_${GN_HOST_ARCH}"

OUT_DIR = "${S}/sdk/out"

do_configure() {

    cd ${S}

    # prevent tmp path warning
    cp ${WORKDIR}/gcc_toolchain.gni.in sdk/build/toolchain/gcc_toolchain.gni
    sed -i "s|@DEBUG_FLAGS@|${DEBUG_FLAGS}|g" sdk/build/toolchain/gcc_toolchain.gni

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
    python3 ./sdk/tools/gn.py ${GN_ARGS}
}
do_configure[depends] += " \
    depot-tools-native:do_populate_sysroot \
    "

do_compile() {

    cd ${S}/sdk

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
