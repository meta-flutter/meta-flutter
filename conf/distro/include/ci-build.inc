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
DISTRO_FEATURES_remove = "sysvinit usbgadget ptest xen"
DISTRO_FEATURES_append = " systemd x11 wayland opengl pam polkit pipewire pulseaudio"

DISTRO_FEATURES_BACKFILL_CONSIDERED = ""

# ***************************************
# CI clones every layer at master. oe-core master tightened the license-format
# QA to require & rather than AND, and meta-openembedded master has not caught
# up: coremark, coremark-pro, glmark2 and lmbench all trip it. That is a fatal
# QA error at parse time, so bitbake halts before reaching any meta-flutter
# recipe. None of those recipes are built here, so demote the check to a warning
# rather than let third-party layer skew block CI entirely. This file is CI-only
# and does not affect QA for meta-flutter's own recipes in a real build.
ERROR_QA_remove = "license-format"
WARN_QA_append = " license-format"

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
PACKAGECONFIG_remove_pn-flutter-engine = " debug profile"

# ***************************************
IMAGE_INSTALL_append = " \
              ffmpeg \
              "

# ***************************************
# Disable SPDX generation to avoid do_create_spdx task failures
# when using shared sstate and allarch packages
INHERIT_remove = "create-spdx"
