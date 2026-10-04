#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# The C++ standard library, for recipes whose output links against another's.
#
# An ABI contract, not a preference. firebase-cpp-sdk hands static archives to
# the hook libraries flutter-app-native builds, and a recipe that disagrees here
# produces objects whose std:: entry points are mangled for the other one: the
# link fails on every function taking a std::string, or it does not and fails at
# dlopen instead.
#
# Separate from flutter-clang-toolchain.inc because the compiler and the
# standard library are separate decisions. Several recipes in this layer want
# clang and have never been built against libc++; moving one here is a change to
# prove, not a tidy-up. See meta-flutter#1124.
#

require conf/include/flutter-clang-toolchain.inc

TC_CXX_RUNTIME = "llvm"

# -stdlib=libc++ explicitly, not through LIBCPLUSPLUS alone.
#
# meta-clang's clang.bbclass appends LIBCPLUSPLUS to CXXFLAGS and LDFLAGS, so
# setting it is enough where clang comes from there. oe-core, which carries
# clang on the newer branches, names LIBCPLUSPLUS only inside clang_git.bb's own
# nativesdk override and nothing else consumes it -- so on those branches the
# variable is inert and the build uses clang against libstdc++, which looks
# right and produces archives with the other mangling. Measured on
# firebase-cpp-sdk: LIBCPLUSPLUS alone gave 506 std::__cxx11 symbols and no
# std::__1; the flag gives 668 std::__1 and none.
#
# Both, so each branch gets it by whichever route it has. On a meta-clang branch
# the flag lands twice, which is harmless.
LIBCPLUSPLUS = "-stdlib=libc++"
CXXFLAGS += "-stdlib=libc++"
