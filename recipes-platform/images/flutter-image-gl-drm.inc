SUMMARY = "A very basic DRM image"

IMAGE_FEATURES += "splash package-management ssh-server-dropbear hwcodecs"

LICENSE = "MIT"

inherit core-image features_check

REQUIRED_DISTRO_FEATURES = "opengl"

CORE_IMAGE_BASE_INSTALL += ""

CLANGSDK = "1"