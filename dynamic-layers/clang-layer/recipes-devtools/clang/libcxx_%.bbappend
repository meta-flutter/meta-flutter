#
# Copyright (c) 2026 Joel Winarske. All rights reserved.
#
# Build libc++ with a static ABI library, matching kirkstone.
#
# flutter-engine's ABI interop gate requires libflutter_engine.so to carry no
# C++ runtime in DT_NEEDED: the engine is built with clang and an in-tree
# libc++, but ships into userlands built with GCC, so a shared libc++ is a
# dependency the image will not have. Without this the armv7 build links one
# and the gate stops it, correctly:
#
#   [rule 2] .../libflutter_engine.so: links a C++ runtime: libc++.so.1
#
# meta-clang carries -DLIBCXX_ENABLE_STATIC_ABI_LIBRARY=ON from kirkstone-clang18
# onward; neither the dunfell nor the dunfell-clang14 branch has it.
#
# Underscore override syntax throughout this layer: BB_VERSION here is 1.46.0,
# which has no colon form and would silently apply nothing -- which is how the
# upstream recipe's own colon-form appends already behave on this release.
#
EXTRA_OECMAKE_append_class-target = " -DLIBCXX_ENABLE_STATIC_ABI_LIBRARY=ON"
