#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# Declare clang-native as the provider of lld-native.
#
# flutter-app-native.bbclass carries `DEPENDS_append = " lld-native"`, which
# dart needs for the native-assets hook. On master that is satisfied by
# oe-core's standalone recipes-devtools/clang/lld_git.bb. meta-clang has no
# such recipe on any release branch: it builds lld inside clang_git.bb through
#
#   LLVM_PROJECTS ?= "clang;clang-tools-extra;lld${LLDB}"
#
# and installs the binary, but never names it in PROVIDES -- only llvm-native.
# Without this, any recipe inheriting flutter-app-native fails at dependency
# resolution with "Nothing PROVIDES 'lld-native'".
#
# Underscore override syntax: bitbake 1.46 has no colon form.
#
PROVIDES_append_class-native = " lld-native"
