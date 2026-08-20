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

DISTRO_FEATURES_BACKFILL_CONSIDERED = ""

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
LICENSE_FLAGS_WHITELIST += "commercial"

# ***************************************
PACKAGECONFIG:remove:pn-flutter-engine = " debug profile"

# ***************************************
IMAGE_INSTALL:append = " \
              ffmpeg \
              "

# ***************************************
# Disable SPDX generation to avoid do_create_spdx task failures
# when using shared sstate and allarch packages
INHERIT:remove = "create-spdx"

# ***************************************
# Cap parallelism by memory, not by core count.
#
# clang-native and llvm-native are the memory peak of a cold build. At one job
# per core they get far enough to be expensive and then die:
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
# The limit that matters is memory per job, not cores. A 32-core/60G runner is
# 1.9G per job at full width, which is under what these translation units want;
# a 48-core/128G runner is 2.7G, still short of it. Deriving the cap from RAM
# gives both machines a working figure from one setting, and any future runner
# the same, where a fixed number would either waste the larger host or keep
# OOM-ing the smaller one. It also survives the workflows' "rm -rf build/conf",
# which is why this is not per-host tuning in local.conf.
#
#   32 cores /  60G -> 15 general, 7 LLVM
#   48 cores / 128G -> 32 general, 16 LLVM
#
# Both are read from the host rather than the cgroup, which is correct here:
# the CI containers are started without --memory or --cpus, so they see all of
# it. Override CI_BUILD_JOBS or CI_BUILD_JOBS_LLVM to pin a runner by hand.
CI_BUILD_CPUS ??= "${@len(os.sched_getaffinity(0))}"
CI_BUILD_MEM_GB ??= "${@os.sysconf('SC_PAGE_SIZE') * os.sysconf('SC_PHYS_PAGES') // 1024 ** 3}"

CI_BUILD_JOBS ??= "${@max(1, min(int(d.getVar('CI_BUILD_CPUS')), int(d.getVar('CI_BUILD_MEM_GB')) // 4))}"
CI_BUILD_JOBS_LLVM ??= "${@max(1, min(int(d.getVar('CI_BUILD_CPUS')), int(d.getVar('CI_BUILD_MEM_GB')) // 8))}"

BB_NUMBER_THREADS = "${CI_BUILD_JOBS}"
PARALLEL_MAKE = "-j ${CI_BUILD_JOBS}"

PARALLEL_MAKE_pn-clang-native = "-j ${CI_BUILD_JOBS_LLVM}"
PARALLEL_MAKE_pn-llvm-native = "-j ${CI_BUILD_JOBS_LLVM}"
PARALLEL_MAKE_pn-highway = "-j ${CI_BUILD_JOBS_LLVM}"
