#
# Copyright (c) 2020-2026 Joel Winarske. All rights reserved.
#

# The v3 embedder builds against libc++/compiler-rt and links with lld. On
# master those come from openembedded-core, which now carries clang; scarthgap's
# openembedded-core predates that move, so they are meta-clang's to provide and
# belong here. A dependency the recipe cannot satisfy without this layer stops
# it parsing at all, rather than merely building differently.
#
# meta-clang has no separate lld recipe -- lld is built inside clang -- so
# ld.lld arrives with clang-native rather than an lld-native of its own:
#
#   ERROR: Nothing PROVIDES 'lld-native' ... Close matches: llvm-native
DEPENDS += "\
    compiler-rt \
    libcxx \
    clang-native \
    "
