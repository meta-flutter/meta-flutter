#
# CI Build Configuration
#

# ***************************************
INHERIT += "rm_work"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

# ***************************************
DL_DIR = "/home/dev/dl"
SSTATE_DIR = "/home/dev/sstate"

INIT_MANAGER = "systemd"

# ***************************************
DISTRO_FEATURES:remove = "sysvinit usbgadget ptest xen"
DISTRO_FEATURES:append = " systemd x11 wayland opengl pam polkit pipewire pulseaudio"

DISTRO_FEATURES_OPTED_OUT = ""

# ***************************************
# CI clones every layer at master. oe-core master tightened the license-format
# QA to require & rather than AND, and meta-openembedded master has not caught
# up: coremark, coremark-pro, glmark2 and lmbench all trip it. That is a fatal
# QA error at parse time, so bitbake halts before reaching any meta-flutter
# recipe. None of those recipes are built here, so demote the check to a warning
# rather than let third-party layer skew block CI entirely. This file is CI-only
# and does not affect QA for meta-flutter's own recipes in a real build.
ERROR_QA:remove = "license-format"
WARN_QA:append = " license-format"

# meta-openembedded master is currently out of step with oe-core master in ways
# that halt parsing before any meta-flutter recipe is reached, and meta-oe has no
# blacksail branch to pin to instead. None of these recipes are used here, so
# mask them rather than let unrelated layer skew block CI:
#
#   recipes-benchmark    coremark, coremark-pro, glmark2, lmbench -- LICENSE
#                        uses AND, which oe-core's license-format QA now rejects
#   zabbix, turbostat,   reference oe.kernel.*, which oe-core no longer makes
#   vboxguestdrivers     available to recipes (ExpansionError on KERNEL_VERSION).
#                        These are the only three users in meta-openembedded, so
#                        this list is complete rather than incremental. All are
#                        kernel-dependent and meaningless against linux-dummy.
#
# Revisit once meta-oe catches up; this is upstream breakage, not a meta-flutter
# issue, and the mask should shrink rather than grow.
BBMASK += "meta-openembedded/meta-oe/recipes-benchmark/"
BBMASK += "meta-openembedded/meta-oe/recipes-connectivity/zabbix/"
BBMASK += "meta-openembedded/meta-oe/recipes-kernel/turbostat/"
BBMASK += "meta-openembedded/meta-oe/recipes-support/vboxguestdrivers/"

PREFERRED_PROVIDER_virtual/kernel = "linux-dummy"

# ***************************************
LICENSE_FLAGS_ACCEPTED += "commercial"

# ***************************************
PACKAGECONFIG:remove:pn-flutter-engine = " debug profile"

# ***************************************
IMAGE_INSTALL:append = " \
              ffmpeg \
              flatpak \
              "

# ***************************************
# Disable SPDX generation to avoid do_create_spdx task failures
# when using shared sstate and allarch packages
INHERIT:remove = "create-spdx"

# ***************************************
# Cap the LLVM builds' parallelism.
#
# clang-native and llvm-native are the memory peak of a cold build. At the
# runner's default of one job per core they get far enough to be expensive and
# then die:
#
#   g++: fatal error: Killed signal terminated program cc1plus
#
# That is the OOM killer, not a compiler fault. It cost three of four jobs on a
# release branch whose sstate was nearly empty, and one on master. Anything with
# a populated sstate restores these instead of building them, which is why it
# surfaces on first builds and hides afterwards.
#
# Capping only the two recipes with evidence against them was too narrow: with
# LLVM held back, highway became the peak and was killed the same way, its
# assembler input truncated mid-write:
#
#   {standard input}: Error: open CFI at the end of file
#   x86_64-oe-linux-g++: fatal error: Killed signal terminated program cc1plus
#
# highway instantiates the same templates across many instruction sets, so its
# translation units rival LLVM's. Chasing recipes one at a time just moves the
# peak, so cap globally and keep the tighter LLVM limit as an exception.
#
# 32 cores against 60G is roughly 1.9G per job at full width, which is under
# what these translation units want. 16 doubles that headroom and still uses
# the machine.
BB_NUMBER_THREADS = "16"
PARALLEL_MAKE = "-j 16"

PARALLEL_MAKE:pn-clang-native = "-j 8"
PARALLEL_MAKE:pn-llvm-native = "-j 8"
PARALLEL_MAKE:pn-highway = "-j 8"
