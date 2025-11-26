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
