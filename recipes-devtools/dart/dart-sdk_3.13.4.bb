#
# Copyright (c) 2020-2023 Joel Winarske
#
# SPDX-License-Identifier: MIT
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

RDEPENDS_dart-sdk_libc-musl = "\
    musl \
    "

S = "${WORKDIR}/gn"

SRCREV = "b530c21f7de367b94fb04787bfed9d8e989d75e8"
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI = " \
    gn://github.com/dart-lang/sdk.git;gn_name=sdk \
    file://gcc_toolchain.gni.in \
    file://0001-build-Make-SDK-artifacts-independent-of-the-builder-s.patch;patchdir=${S}/sdk \
"

inherit gn-fetcher pkgconfig deploy

# gn writes its output inside the sync directory; keep it out of the
# cached tarball, which is also what a mirror would serve.
GN_PACK_EXCLUDES = "./sdk/out"

require conf/include/gn-utils.inc

# For gn.bbclass
EXTRA_GN_SYNC ?= "--shallow --no-history -R -D"


# Dart 3.13.1's tools/gn.py dropped --platform-sdk and --use-mallinfo2. argparse
# exits 2 on an unrecognized option, so leaving them here fails do_configure
# immediately. The PACKAGECONFIG entries are removed rather than defaulted off,
# since enabling them could only ever break the build.
PACKAGECONFIG ??= "verify-sdk-hash dynamic-modules"

PACKAGECONFIG[verify-sdk-hash] = "--verify-sdk-hash"
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
GN_ARGS_append = " --mode product"

GN_HOST_ARCH = "${@gn_host_arch_name(d)}"

# available architectures
# all,ia32,x64,
# arm,arm64,arm_x64,arm_arm64,
# simarm,simarm64,simarm_x64,simarm_arm64,x64c,arm64c,simarm64c,
# simriscv32,simriscv64,simx64,simx64c,riscv32,riscv64

# --arm-float-abi [soft,softfp,hard]

GN_ARGS_append_armv7 = " -a arm_${GN_HOST_ARCH} --arm-float-abi ${TARGET_FPU}"
GN_ARGS_append_armv7a = " -a arm_${GN_HOST_ARCH} --arm-float-abi ${TARGET_FPU}"
GN_ARGS_append_armv7ve = " -a arm_${GN_HOST_ARCH} --arm-float-abi ${TARGET_FPU}"
GN_ARGS_append_aarch64 = " -a arm64_${GN_HOST_ARCH}"
GN_ARGS_append_x86-64 = " -a x64_${GN_HOST_ARCH}"
GN_ARGS_append_riscv32 = " -a riscv32_${GN_HOST_ARCH}"
GN_ARGS_append_riscv64 = " -a riscv64_${GN_HOST_ARCH}"

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

INSANE_SKIP_${PN} = "already-stripped ldflags"

FILES_${PN} += "${datadir}"

# The cross gen_snapshot, for dart-sdk-tools to stage.
#
# A cross build puts the target binaries in the mode root and the host ones
# under clang_${GN_HOST_ARCH}/. The host gen_snapshot runs on the build machine
# and emits code for the target --
#
#   $ out/ProductXARM64_X64/clang_x64/gen_snapshot --version
#   Dart SDK version: 3.13.4 (stable) on "linux_simarm64"
#
# -- which is what building a Dart executable for the target needs, and it was
# discarded at the end of the build until now. Deployed rather than packaged: a
# build-machine binary has no business in a target package, as #1009 worked out
# the hard way. Per MACHINE, because it is built for one target.
do_deploy() {
    # Target builds only. The native and nativesdk variants build for the host,
    # so they have no cross tools to offer, and deploying from them writes the
    # same files as the target build, which sstate refuses.
    case "${PN}" in
        *-native|nativesdk-*)
            bbnote "host build: no cross tools to deploy"
            return 0
            ;;
    esac

    BUILD_DIR="${OUT_DIR}/$(ls ${OUT_DIR})"
    host_dir="${BUILD_DIR}/clang_${GN_HOST_ARCH}"

    install -d ${DEPLOYDIR}/dart-sdk-tools

    # No clang_ subdirectory when host and target share an architecture: the
    # binaries in the mode root already run on the builder.
    if [ ! -d "$host_dir" ]; then
        bbnote "no $host_dir: host and target share an architecture"
        host_dir="${BUILD_DIR}"
    fi

    cd "$host_dir"
    for f in gen_snapshot gen_snapshot_product; do
        test -f "$f" && install -m 0755 "$f" ${DEPLOYDIR}/dart-sdk-tools/
    done
}
addtask deploy after do_install before do_build

BBCLASSEXTEND = "native nativesdk"