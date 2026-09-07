#
# SPDX-FileCopyrightText: (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# meta-oe builds capnproto static and non-PIC, so linking capnp or kj into a
# shared library fails on kj/exception.c++'s thread-local:
#
#   libkj.a(exception.c++.o): relocation R_X86_64_TPOFF32 against
#   `kj::(anonymous namespace)::threadLocalCallback' can not be used when making
#   a shared object; local-exec is incompatible with -shared
#
# test-runner hits this: libTestRunnerClient is SHARED and links both. Code model
# only -- still static, no .so, no new packages -- but capnproto's sstate is
# invalidated. Belongs in meta-oe; drop this when it lands there.
#
EXTRA_OECMAKE:append = " -DCMAKE_POSITION_INDEPENDENT_CODE=ON"

# find_package(CapnProto) fails against the target sysroot: CapnProtoTargets.cmake
# exports imported targets for capnp_tool, capnpc_cpp and capnpc_capnp, and Yocto
# does not stage ${bindir} for target recipes, so CMake reports the package as
# "faulty ... but not all the files it references". test-runner's
# find_package(CapnProto REQUIRED) is where it surfaces.
#
# meta-oe carries this patch already; it arrived after this branch. Native still
# installs and exports the binaries, so capnproto-native keeps providing the capnp
# the build actually runs -- the patch keys off CMAKE_CROSSCOMPILING, so a native
# build is unchanged. Cross installs them without exporting. Applied to every
# variant, as meta-oe does, so nativesdk is covered too.
#
# Applies to 1.0.2, which is the only capnproto this branch's meta-oe has.
# Belongs in meta-oe; drop this when it lands there.
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI:append = " file://0001-Export-binaries-only-for-native-build.patch"
