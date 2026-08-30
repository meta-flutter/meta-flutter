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

# license-format is deliberately left at its oe-core default, which is fatal.
# It used to be demoted here, because meta-openembedded master declares
# LicenseRef- values oe-core cannot resolve and that halts parsing before any
# meta-flutter recipe is reached. But every recipe that does so -- coremark,
# coremark-pro, glmark2, lmbench -- lives under recipes-benchmark, which the
# BBMASK below already removes from the parse entirely, so the demotion was
# redundant with a narrower fix already in place.
#
# It was not harmless, either: ERROR_QA:remove applies to the whole build, so a
# genuine license-format error in one of meta-flutter's own recipes surfaced as
# a warning in a green CI run. That is the check that would have answered the
# question in #823 directly. Keep the mask narrow and the check fatal.

# meta-openembedded master is currently out of step with oe-core master in ways
# that halt parsing before any meta-flutter recipe is reached, and meta-oe has no
# blacksail branch to pin to instead. None of these recipes are used here, so
# mask them rather than let unrelated layer skew block CI:
#
#   recipes-benchmark    coremark, coremark-pro, glmark2, lmbench -- LICENSE
#                        names a LicenseRef- that oe-core cannot resolve
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

PARALLEL_MAKE:pn-clang-native = "-j ${CI_BUILD_JOBS_LLVM}"
PARALLEL_MAKE:pn-llvm-native = "-j ${CI_BUILD_JOBS_LLVM}"
PARALLEL_MAKE:pn-highway = "-j ${CI_BUILD_JOBS_LLVM}"
