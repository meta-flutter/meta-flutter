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

SRCREV = "fa8c1270c374c1346931583a17c3b7f278d3822d"
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI = " \
    gn://github.com/dart-lang/sdk.git;gn_name=sdk \
    file://gcc_toolchain.gni.in \
    file://0001-build-config-target-sysroot-for-the-target-half-only.patch;patchdir=${S}/sdk \
"

inherit gn-fetcher pkgconfig deploy

# gn writes its output inside the sync directory; keep it out of the
# cached tarball, which is also what a mirror would serve.
GN_PACK_EXCLUDES = "./sdk/out"

require conf/include/gn-utils.inc
require conf/include/host-tools.inc

# For gn.bbclass
EXTRA_GN_SYNC ?= "--shallow --no-history -R -D"


# Dart 3.13.1's tools/gn.py dropped --platform-sdk and --use-mallinfo2. argparse
# exits 2 on an unrecognized option, so leaving them here fails do_configure
# immediately. The PACKAGECONFIG entries are removed rather than defaulted off,
# since enabling them could only ever break the build.
PACKAGECONFIG ??= "verify-sdk-hash dynamic-modules"

# dart_dynamic_modules is off upstream: runtime_args.gni defaults it false
# and tools/gn.py defaults --dart-dynamic-modules to False. It is on here,
# and the bytecode interpreter's FFI trampoline is not 32-bit clean --
# FfiCallArguments carries register values in uword, so runtime/vm/
# runtime_entry.cc fails to build a double out of one:
#
#   runtime_entry.cc:1406:26: error: no matching function for call to 'bit_cast'
#   note: because 'sizeof(double) == sizeof(unsigned int)' (8 == 4)
#
# The field was uint64_t before the 3.14.0 refactor. Nothing upstream hits
# this because the feature ships off. Drop it where it does not build.
PACKAGECONFIG_remove_arm = "dynamic-modules"
PACKAGECONFIG_remove_riscv32 = "dynamic-modules"

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

# The whole -a value, "<target>_<host>" for a cross build. Also the key
# DART_USE_SYSROOT has to carry below, so it lives in one place.
DART_GN_ARCH_armv7 = "arm_${GN_HOST_ARCH}"
DART_GN_ARCH_armv7a = "arm_${GN_HOST_ARCH}"
DART_GN_ARCH_armv7ve = "arm_${GN_HOST_ARCH}"
DART_GN_ARCH_aarch64 = "arm64_${GN_HOST_ARCH}"
DART_GN_ARCH_x86-64 = "x64_${GN_HOST_ARCH}"
DART_GN_ARCH_riscv32 = "riscv32_${GN_HOST_ARCH}"
DART_GN_ARCH_riscv64 = "riscv64_${GN_HOST_ARCH}"

GN_ARGS_append_armv7 = " -a ${DART_GN_ARCH} --arm-float-abi ${TARGET_FPU}"
GN_ARGS_append_armv7a = " -a ${DART_GN_ARCH} --arm-float-abi ${TARGET_FPU}"
GN_ARGS_append_armv7ve = " -a ${DART_GN_ARCH} --arm-float-abi ${TARGET_FPU}"
GN_ARGS_append_aarch64 = " -a ${DART_GN_ARCH}"
GN_ARGS_append_x86-64 = " -a ${DART_GN_ARCH}"
GN_ARGS_append_riscv32 = " -a ${DART_GN_ARCH}"
GN_ARGS_append_riscv64 = " -a ${DART_GN_ARCH}"

OUT_DIR = "${S}/sdk/out"

do_configure() {

    cd ${S}

    # prevent tmp path warning
    cp ${WORKDIR}/gcc_toolchain.gni.in sdk/build/toolchain/gcc_toolchain.gni
    sed -i "s|@DEBUG_FLAGS@|${DEBUG_FLAGS}|g" sdk/build/toolchain/gcc_toolchain.gni

    # we only build one mode type
    rm -rf "${OUT_DIR}" || true

    # gn.py reads this as an arch-keyed map, "<arch>=<path>", and drops
    # anything that does not parse as one. TARGET_SYSROOT is not a variable
    # this release defines, so what went out before was empty, gn.py decided
    # no sysroot had been given, and the build took Dart's own bundled one.
    # Skip it on a machine with no DART_GN_ARCH rather than send a bare "=".
    # One gn arg cannot cover both halves of a cross build, so the sysroot.gni
    # patch is what keeps this off the host toolchain.
    if [ -n "${DART_GN_ARCH}" ]; then
        export DART_USE_SYSROOT="${DART_GN_ARCH}=${STAGING_DIR_TARGET}"
    fi
    # Inert, and left that way for now: gn.py keys this one by plain cpu
    # name, and the clang toolchains it picks ignore the prefix regardless.
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

    # Keyed and guarded as in do_configure, since ninja re-runs gn when the
    # args change.
    if [ -n "${DART_GN_ARCH}" ]; then
        export DART_USE_SYSROOT="${DART_GN_ARCH}=${STAGING_DIR_TARGET}"
    fi
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
#
# The path is conf/include/host-tools.inc's so this and dart-sdk-tools cannot
# disagree about it, and the layout token is in vardeps so moving it re-runs
# this task.
do_deploy[sstate-outputdirs] = "${DART_SDK_HOST_TOOLS_DIR}"
do_deploy[vardeps] += "FLUTTER_HOST_TOOLS_LAYOUT"
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