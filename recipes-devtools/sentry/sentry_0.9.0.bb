#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Sentry SDK for C, C++ and native applications."
DESCRIPTION = "Sentry Native SDK"
AUTHOR = "Sentry"
HOMEPAGE = "https://github.com/getsentry/sentry-native"
BUGTRACKER = "https://github.com/getsentry/sentry-native/issues"
SECTION = "devtools"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=16ed57a2dfbb53c2aa8807af4e5d44da"

DEPENDS += "\
    curl \
    "

SRCREV ??= "6129d36d717b77d53c8af8fe439ed0370fb63ea4"
SRC_URI = " \
    gitsm://github.com/getsentry/sentry-native.git;protocol=https;branch=master \
    file://0001-version-SO.patch \
"

S = "${WORKDIR}/git"

inherit cmake pkgconfig 

EXTRA_OECMAKE += "\
    -D SENTRY_BUILD_TESTS=OFF \
    -D SENTRY_BUILD_EXAMPLES=OFF \
    -D SENTRY_BUILD_FORCE32=OFF \
"

# sentry-native defaults to the crashpad backend on Linux, and crashpad asserts
# that std::atomic<bool> is lock-free so its spin guard is signal-safe:
#
#   crashpad/util/synchronization/scoped_spin_guard.h:38:36: error:
#     static assertion failed: std::atomic<bool> may not be signal-safe
#
# On riscv64 that assertion is false with this release's GCC 11. RISC-V has no
# native sub-word atomics, and inlining them (-minline-atomics) only arrived
# after GCC 12 branched, so 1-byte atomics become libatomic calls and
# is_always_lock_free reports false. Newer releases carry a GCC that inlines
# them and build crashpad fine, which is why this override is scoped to the
# architecture rather than applied everywhere.
#
# inproc keeps crash reporting without crashpad's out-of-process handler, so
# riscv64 gets a working sentry rather than no sentry at all.
EXTRA_OECMAKE:append:riscv64 = " -D SENTRY_BACKEND=inproc"
