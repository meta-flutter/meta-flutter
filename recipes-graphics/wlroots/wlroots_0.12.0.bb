LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=7578fad101710ea2d289ff5411f1b818"

PV = "0.12.0"
SRC_URI = "git://github.com:/swaywm/wlroots.git;protocol=https;tag=${PV}"

DEPENDS = " wayland-native wayland virtual/egl libdrm mesa libinput libxkbcommon libudev pixman cmake"


# gbm = dependency('gbm', version: '>=17.1.0')

REQUIRED_DISTRO_FEATURES = "wayland wayland"

S = "${WORKDIR}/git"

inherit meson pkgconfig distro_features_check

do_configure_prepend() {
    export WLR_BACKENDS="drm,libinput,wayland"
}
