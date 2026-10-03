#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# The C++ standard library the layer's Flutter stack is built against.
#
# One place because it is an ABI contract, not a preference. These pieces link
# into one process: flutter-app-native builds a native-assets hook library,
# firebase-cpp-sdk hands it static archives, and the embedder loads the result.
# A recipe that disagrees about the standard library produces objects whose
# std:: entry points are mangled for the other one, and the link fails on every
# function that takes a std::string -- or worse, does not, and fails at dlopen.
#
# Settings only, deliberately. Who provides compiler-rt, libcxx and lld differs
# by release: oe-core carries them on the newer branches, and before that they
# are meta-clang's, where lld is built inside clang rather than being its own
# recipe. So DEPENDS belongs with the recipe, or in its
# dynamic-layers/clang-layer bbappend on a branch that needs meta-clang for it.
#

TOOLCHAIN = "clang"
TC_CXX_RUNTIME = "llvm"

# -stdlib=libc++ explicitly, not through LIBCPLUSPLUS.
#
# Setting that variable leaves the flag in neither CXX nor CXXFLAGS, and the
# build then uses clang against libstdc++ -- which looks correct and produces
# archives with the other mangling. Measured on firebase-cpp-sdk: with
# LIBCPLUSPLUS alone, 506 std::__cxx11 symbols and no std::__1; with the flag
# here, 668 std::__1 and no __cxx11. See meta-flutter#1124.
CXXFLAGS += "-stdlib=libc++"
