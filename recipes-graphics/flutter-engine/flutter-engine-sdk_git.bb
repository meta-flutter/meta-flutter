#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "Flutter engine host tools"
DESCRIPTION = "gen_snapshot and the rest of the engine's build-machine tools, \
staged where a build-machine binary belongs. The engine builds them as a \
by-product of its target build and deploys them; this recipe is the common \
place everything else pulls them from. See meta-flutter#1009."
AUTHOR = "Joel Winarske"
HOMEPAGE = "https://github.com/flutter/flutter"
SECTION = "graphics"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"

require conf/include/flutter-version.inc
require conf/include/host-tools.inc

PV = "${@get_flutter_sdk_version(d)}"

INHIBIT_DEFAULT_DEPS = "1"

# Nothing to fetch or compile: the engine's do_deploy is the source, and
# do_install below is the whole recipe.
do_fetch[noexec] = "1"
do_unpack[noexec] = "1"
do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

# The engine is built per MACHINE; its host tools are not, but they arrive
# through a MACHINE-specific build all the same. Tie this recipe's signature to
# the engine's revision so a roll rebuilds it rather than handing back tools
# from the previous one out of sstate.
FLUTTER_ENGINE_SRCREV = "${@get_flutter_hash(d)}"
# MACHINE too: the tools are built for a target, so the set staged here is
# the one the engine built for this machine, not a machine-independent copy.
do_install[vardeps] += "FLUTTER_ENGINE_SRCREV MACHINE FLUTTER_HOST_TOOLS_LAYOUT"
do_install[depends] += "flutter-engine:do_deploy"

FLUTTER_SDK_TOOLS_DIR = "${bindir}/flutter-engine-sdk/${PV}"

do_install() {
    src="${FLUTTER_ENGINE_HOST_TOOLS_DIR}/flutter-engine-sdk/${PV}"

    if [ ! -d "$src" ]; then
        bbfatal "flutter-engine deployed no host tools at $src"
    fi

    for mode in $(ls "$src"); do
        install -d ${D}${FLUTTER_SDK_TOOLS_DIR}/$mode
        # tools only. args.gn sits beside them in the deploy tree for
        # inspection and carries absolute TMPDIR paths, so it is not packaged.
        for tool in "$src/$mode"/*; do
            case "$tool" in
                *.gn) continue ;;
            esac
            install -m 0755 "$tool" ${D}${FLUTTER_SDK_TOOLS_DIR}/$mode/
        done
    done
}

FILES_${PN} = "${FLUTTER_SDK_TOOLS_DIR}"

# These come out of the engine's own build and link the build host's libc, so
# the nativesdk package's ELF requirements -- libm.so.6(GLIBC_2.29) and the
# rest -- have to be satisfied by the SDK's glibc. Declaring it is the fix.
# INSANE_SKIP for file-rdeps would only move the failure from the build to an
# SDK user's machine, and uninative does not help: it rewrites interpreters for
# native, cross and crosssdk only. Not exercised here, since nothing in CI
# builds a nativesdk package -- it wants a populate_sdk and an install to
# confirm the binaries run under the SDK's loader. See #1197.
RDEPENDS_${PN}_class-nativesdk = "nativesdk-glibc"

# Build-machine binaries. The target variant would put them in an image that
# may not share the builder's architecture, which is the bug this recipe
# exists to fix; flutter-engine itself ships them on a matching-arch image.
COMPATIBLE_HOST_class-target = "null"

BBCLASSEXTEND = "native nativesdk"
