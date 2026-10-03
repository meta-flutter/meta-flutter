#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# The recipe requires conf/include/flutter-clang-toolchain.inc for libc++,
# because its hook library links firebase-cpp-sdk's static archives and the two
# have to agree. On this branch those runtimes are meta-clang's rather than
# oe-core's, and meta-clang appends LIBCPLUSPLUS to the flags without adding
# libcxx to DEPENDS, so name them here. A DEPENDS the recipe cannot satisfy
# stops it parsing rather than merely building differently.
DEPENDS += "\
    compiler-rt \
    libcxx \
    "
