#
# Fix HWCAP2 macro redefinition that prevents libwebrtc from compiling
# for ARM/AArch64 when the sysroot's bits/hwcap-32.h already defines
# these macros.
#
# Scoped to aarch64 and arm targets only — x86_64 builds are unaffected
# and must not receive this patch (fixes breakage from PR #748).
#
# Fixes: https://github.com/meta-flutter/meta-flutter/issues/645
#

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append:aarch64 = " file://0001-fix-boringssl-hwcap2-redefined-arm.patch"
SRC_URI:append:arm = " file://0001-fix-boringssl-hwcap2-redefined-arm.patch"
