#
# Copyright (c) 2026 Joel Winarske. All rights reserved.
#
# Declare clang-native as the provider of lld-native.
#
# flutter-app-native.bbclass carries `DEPENDS:append = " lld-native"`, which
# dart needs for the native-assets hook. On master that is satisfied by
# oe-core's standalone recipes-devtools/clang/lld_git.bb, which carries
# BBCLASSEXTEND = "native". No meta-clang release branch has that recipe:
# kirkstone-clang18 and scarthgap both build lld inside clang_git.bb, through
#
#   LLVM_PROJECTS ?= "clang;clang-tools-extra;lld${LLDB}"
#
# and install the binary, but never name it in PROVIDES. So any recipe
# inheriting flutter-app-native fails at dependency resolution:
#
#   ERROR: Nothing PROVIDES 'lld-native' (but ...flathub-catalog_0.4.2.bb
#     DEPENDS on or otherwise requires it). Close matches:
#     db-native  gd-native  llvm-native
#
# clang-native builds and installs ld.lld, so pointing lld-native at it names
# the recipe that already produces the linker rather than stubbing the
# dependency out.
#
PROVIDES:append:class-native = " lld-native"
