#
# Fix HWCAP2 macro redefinition that prevents libwebrtc from compiling
# for ARM when the sysroot's bits/hwcap-32.h already defines these macros.
#
# Fixes: https://github.com/meta-flutter/meta-flutter/issues/645
#

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI_EXTRA += "\
    file://0001-fix-boringssl-hwcap2-redefined-arm.patch \
    "
