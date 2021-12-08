SUMMARY = "A very basic Wayland image"

IMAGE_FEATURES += "splash package-management ssh-server-dropbear hwcodecs"

LICENSE = "MIT"

inherit core-image features_check

REQUIRED_DISTRO_FEATURES = "opengl wayland"

CORE_IMAGE_BASE_INSTALL:append = " weston weston-init"

CLANGSDK = "1"