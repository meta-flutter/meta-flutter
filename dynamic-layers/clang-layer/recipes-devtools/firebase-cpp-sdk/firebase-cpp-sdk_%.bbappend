#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# conf/include/flutter-clang-toolchain.inc builds this with clang and libc++,
# because its static archives link into the hook libraries flutter-app-native
# builds and the two have to agree on the standard library. On the newer
# branches oe-core carries those runtimes and the recipe can name them itself;
# here they are meta-clang's, so they belong in this layer. A DEPENDS the
# recipe cannot satisfy stops it parsing rather than merely building
# differently, which is why this is a dynamic layer and not an override.
#
# Same shape as ivi-homescreen_3.0.bbappend beside it.
DEPENDS += "\
    compiler-rt \
    libcxx \
    "
