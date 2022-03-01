SUMMARY = "Toyota IVI Homescreen"
DESCRIPTION = "Toyota's Flutter Embedder that communicates with AGL-compositor/Wayland compositors"
AUTHOR = "joel.winarske@toyotaconnected.com"
HOMEPAGE = "https://github.com/toyota-connected/ivi-homescreen"
BUGTRACKER = "https://github.com/toyota-connected/ivi-homescreen/issues"
SECTION = "graphics"
CVE_PRODUCT = "homescreen"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=39ae29158ce710399736340c60147314"

DEPENDS += "\
    compiler-rt \
    libcxx \
    libxkbcommon \
    virtual/egl \
    wayland \
    wayland-native \
    wayland-protocols \
    "

REQUIRED_DISTRO_FEATURES = "wayland opengl"

SRCREV ??= "${AUTOREV}"
SRC_URI = "git://github.com/toyota-connected/ivi-homescreen.git;protocol=https;branch=main \
           file://homescreen.service.in \
          "

S = "${WORKDIR}/git"

inherit cmake features_check pkgconfig systemd

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"

PACKAGECONFIG ??= "transparency gstreamer ${@bb.utils.filter('DISTRO_FEATURES', 'systemd', d)}"

PACKAGECONFIG[transparency] = "-DBUILD_EGL_TRANSPARENCY=ON, -DBUILD_EGL_TRANSPARENCY=OFF"
PACKAGECONFIG[gstreamer] = "-DBUILD_PLUGIN_GSTREAMER=ON, -DBUILD_PLUGIN_GSTREAMER=OFF, gstreamer1.0 gstreamer1.0-plugins-base ffmpeg"

EXTRA_OECMAKE += "-D CMAKE_SYSROOT=${STAGING_DIR_TARGET}/usr"

FLUTTER_RUNTIME ??= "release"

# Use pattern "--a={absolute path to flutter_assets}"
IVI_HOMESCREEN_APP_OVERRIDE ??= ""
# parameters to pass to homescreen app and Dart VM
SERVICE_EXEC_START_PARAMS ??= ""

SERVICE_UNIT        ??= "Requires=weston@root.service\nAfter=weston@root.service\n"
SERVICE_USER_GROUP  ??= "User=root\nGroup=root"
SERVICE_RESTART     ??= "Restart=on-failure\nRestartSec=1"
SERVICE_ENVIRONMENT ??= "Environment=HOME=/home/root\nEnvironment=XDG_RUNTIME_DIR=/run/user/0"
SERVICE_EXEC_START  ??= "ExecStart=/usr/bin/homescreen --f ${IVI_HOMESCREEN_APP_OVERRIDE} ${SERVICE_EXEC_START_PARAMS}"
SERVICE_SERVICE     ??= "${SERVICE_USER_GROUP}\n${SERVICE_RESTART}\n${SERVICE_ENVIRONMENT}\n${SERVICE_EXEC_START}\n"
SERVICE_INSTALL     ??= "WantedBy=graphical.target\n"

do_install_append() {
    if ${@bb.utils.contains('PACKAGECONFIG', 'systemd', 'true', 'false', d)}; then
        install -m 644 ${WORKDIR}/homescreen.service.in ${WORKDIR}/homescreen.service
        sed -i "s|@ServiceUnit@|${SERVICE_UNIT}|g" ${WORKDIR}/homescreen.service
        sed -i "s|@ServiceService@|${SERVICE_SERVICE}|g" ${WORKDIR}/homescreen.service
        sed -i "s|@ServiceInstall@|${SERVICE_INSTALL}|g" ${WORKDIR}/homescreen.service
        install -d ${D}${systemd_system_unitdir}
        install -m 644 ${WORKDIR}/homescreen.service ${D}${systemd_system_unitdir}/homescreen.service
    fi
}

SYSTEMD_SERVICE_${PN} += "${@bb.utils.contains('PACKAGECONFIG', 'systemd', 'homescreen.service', '', d)}"
SYSTEMD_PACKAGES = "${@bb.utils.contains('PACKAGECONFIG', 'systemd', '${PN}', '', d)}"
