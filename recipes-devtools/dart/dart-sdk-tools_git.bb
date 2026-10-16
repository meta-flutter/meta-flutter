#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "Dart SDK cross tools"
DESCRIPTION = "gen_snapshot, built to run on the build machine and emit code \
for the target. The dart-sdk build produces it as a by-product and deploys it; \
this recipe is the common place a dart-app build pulls it from. See \
meta-flutter#428."
AUTHOR = "Joel Winarske"
HOMEPAGE = "https://github.com/dart-lang/sdk"
SECTION = "devtools"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"

# No version in the path or the recipe name: these tools come out of whatever
# dart-sdk this configuration built, and pairing a snapshot with a different
# SDK's runtime does not work anyway. One dart-sdk per build, one set here.
PV = "1.0"

INHIBIT_DEFAULT_DEPS = "1"

# Nothing to fetch or build: dart-sdk's do_deploy is the source.
do_fetch[noexec] = "1"
do_unpack[noexec] = "1"
do_patch[noexec] = "1"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

# The tools are built for one target, so the set staged here is the one
# dart-sdk built for this machine. MACHINE in the signature keeps sstate from
# handing back another machine's copy.
do_install[vardeps] += "MACHINE"
do_install[depends] += "dart-sdk:do_deploy"

DART_SDK_TOOLS_DIR = "${bindir}/dart-sdk-tools"

do_install() {
    src="${DEPLOY_DIR_IMAGE}/dart-sdk-tools"

    if [ ! -d "$src" ]; then
        bbfatal "dart-sdk deployed no cross tools at $src"
    fi

    install -d ${D}${DART_SDK_TOOLS_DIR}
    install -m 0755 "$src"/* ${D}${DART_SDK_TOOLS_DIR}/
}

FILES:${PN} = "${DART_SDK_TOOLS_DIR}"

# Build-machine binaries. A target variant would put a foreign ELF in an image
# -- the mistake #1009 caught on kirkstone.
COMPATIBLE_HOST:class-target = "null"

BBCLASSEXTEND = "native nativesdk"
