#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# The compiler the layer's Flutter stack is built with.
#
# One place rather than a line in every recipe, so that "which toolchain does
# this layer use" has one answer and a recipe cannot ask for clang while quietly
# missing whatever else that implies -- which is how the libc++ half came to be
# set in recipes where it had no effect. See meta-flutter#1124.
#
# The standard library is the other half and lives in
# conf/include/flutter-clang-libcxx.inc, which requires this. Recipes whose
# output links against another's -- firebase-cpp-sdk's archives and the hook
# libraries flutter-app-native builds -- need that one. A recipe that only wants
# the compiler needs this.
#
# Providers stay out of both: who ships compiler-rt, libcxx and lld differs by
# release, with oe-core carrying them on the newer branches and meta-clang
# before that, where lld is built inside clang rather than being its own recipe.
# So DEPENDS belongs with the recipe, or in its dynamic-layers/clang-layer
# bbappend on a branch that needs meta-clang for it.
#

TOOLCHAIN = "clang"

# Say so when clang is not actually there.
#
# TOOLCHAIN = "clang" only takes effect where something activates the
# toolchain-clang override: oe-core on the newer branches, meta-clang on the
# older ones. Without it the variable is set and ignored and CXX stays gcc,
# which for a recipe that also wants libc++ ends as
#
#   gcc: error: unrecognized command-line option '-stdlib=libc++'
#
# a poor way to learn that a layer is missing.
#
# SkipRecipe rather than bb.fatal: a parse-time fatal in a recipe halts the
# whole parse, so a configuration that never builds these would stop working
# over a layer it does not need. Skipping makes the recipe unavailable with a
# reason instead, which is what bitbake prints when something asks for it.
# The override is what to test, not CXX. oe-core's classes/toolchain/clang.bbclass
# sets TCOVERRIDE = "toolchain-clang", which lands in OVERRIDES; meta-clang
# appends toolchain-${TOOLCHAIN} from inside its own class, so on the older
# branches it is present only when that layer is. Either way the override says
# whether a clang toolchain is in effect.
#
# Reading CXX instead was wrong twice over: it is a string assembled from a
# wrapper, a prefix, the compiler and flags, so "ccache clang++" made the first
# word ccache and skipped every recipe here (#1155), and icecc, distcc or a
# hand-set CXX would each have done it again.
python () {
    if 'toolchain-clang' not in (d.getVar('OVERRIDES') or '').split(':'):
        raise bb.parse.SkipRecipe(
            "requires clang, and TOOLCHAIN = \"%s\" did not take effect: CXX is "
            "%s. On this release clang may come from meta-clang -- check that "
            "layer is present." % (d.getVar('TOOLCHAIN'), d.getVar('CXX')))
}
