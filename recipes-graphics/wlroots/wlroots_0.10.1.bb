LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7578fad101710ea2d289ff5411f1b818"

SRC_URI = "https://github.com/swaywm/wlroots/releases/download/0.10.1/wlroots-0.10.1.tar.gz"
SRC_URI[md5sum] = "3d9736cfbfaf6661d36b6c788ac2c199"

DEPENDS = " wayland-native wayland mesa libinput libxkbcommon pixman"

REQUIRED_DISTRO_FEATURES = "wayland"


S = "${WORKDIR}/${PN}-${PV}"

inherit meson pkgconfig distro_features_check

do_configure_prepend() {
    export WLR_BACKENDS="headless"
}

