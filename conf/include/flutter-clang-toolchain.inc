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

# Say so when clang is not actually there.
#
# TOOLCHAIN = "clang" only takes effect where something activates the
# toolchain-clang override: oe-core on the newer branches, meta-clang on this
# one. Without that layer the variable is set and ignored, CXX stays gcc, and
# the -stdlib=libc++ above reaches a compiler with no such option:
#
#   gcc: error: unrecognized command-line option '-stdlib=libc++'
#
# which is a poor way to learn that a layer is missing. The DEPENDS that would
# have failed loudly live in dynamic-layers/clang-layer, so they are absent too
# and nothing else complains.
#
# SkipRecipe rather than bb.fatal: a parse-time fatal in a recipe halts the
# whole parse, so a configuration that never builds these would stop working
# over a layer it does not need. Skipping makes the recipe unavailable with a
# reason instead, which is what bitbake prints when something asks for it.
python () {
    cxx = (d.getVar('CXX') or '').split()
    if cxx and 'clang' not in cxx[0]:
        raise bb.parse.SkipRecipe(
            "requires clang, and TOOLCHAIN = \"%s\" did not take effect: CXX is "
            "%s. On this release clang comes from meta-clang -- add that layer."
            % (d.getVar('TOOLCHAIN'), cxx[0]))
}
