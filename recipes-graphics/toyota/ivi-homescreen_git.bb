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

SRCREV ??= "e0ede14ab7fbb013f37a2e62b655c8bedacb0c63"
SRC_URI = "git://github.com/toyota-connected/ivi-homescreen.git;protocol=https;branch=main \
           file://homescreen.service.in \
          "

S = "${WORKDIR}/git"

inherit cmake features_check pkgconfig systemd

require conf/include/flutter-runtime.inc

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER:libgcc = "compiler-rt"

PACKAGECONFIG ??= "gstreamer mouse-cursor package-info secure-storage text-input transparency url-launcher texture-test-egl"

PACKAGECONFIG[accessibility] = "-DBUILD_PLUGIN_ACCESSIBILITY=ON, -DBUILD_PLUGIN_ACCESSIBILITY=OFF"
PACKAGECONFIG[gstreamer] = "-DBUILD_PLUGIN_GSTREAMER_EGL=ON, -DBUILD_PLUGIN_GSTREAMER_EGL=OFF, gstreamer1.0 gstreamer1.0-plugins-base ffmpeg"
PACKAGECONFIG[isolate] = "-DBUILD_PLUGIN_ISOLATE=ON, -DBUILD_PLUGIN_ISOLATE=OFF"
PACKAGECONFIG[mouse-cursor] = "-DBUILD_PLUGIN_MOUSE_CURSOR=ON, -DBUILD_PLUGIN_MOUSE_CURSOR=OFF"
PACKAGECONFIG[navigation] = "-DBUILD_PLUGIN_NAVIGATION=ON, -DBUILD_PLUGIN_NAVIGATION=OFF"
PACKAGECONFIG[texture-test-egl] = "-DBUILD_TEXTURE_TEST_EGL=ON, -DBUILD_TEXTURE_TEST_EGL=OFF"
PACKAGECONFIG[package-info] = "-DBUILD_PLUGIN_PACKAGE_INFO=ON, -DBUILD_PLUGIN_PACKAGE_INFO=OFF"
PACKAGECONFIG[platform] = "-DBUILD_PLUGIN_PLATFORM=ON, -DBUILD_PLUGIN_PLATFORM=OFF"
PACKAGECONFIG[platform-view] = "-DBUILD_PLUGIN_PLATFORM_VIEW=ON, -DBUILD_PLUGIN_PLATFORM_VIEW=OFF"
PACKAGECONFIG[restoration] = "-DBUILD_PLUGIN_RESTORATION=ON, -DBUILD_PLUGIN_RESTORATION=OFF"
PACKAGECONFIG[secure-storage] = "-DBUILD_PLUGIN_SECURE_STORAGE=ON, -DBUILD_PLUGIN_SECURE_STORAGE=OFF, libsecret"
PACKAGECONFIG[text-input] = "-DBUILD_PLUGIN_TEXT_INPUT=ON, -DBUILD_PLUGIN_TEXT_INPUT=OFF"
PACKAGECONFIG[transparency] = "-DBUILD_EGL_TRANSPARENCY=ON, -DBUILD_EGL_TRANSPARENCY=OFF"
PACKAGECONFIG[url-launcher] = "-DBUILD_PLUGIN_URL_LAUNCHER=ON, -DBUILD_PLUGIN_URL_LAUNCHER=OFF"


EXTRA_OECMAKE += "-D CMAKE_SYSROOT=${STAGING_DIR_TARGET}/usr"

FLUTTER_RUNTIME ??= "release"

# Use pattern "--b={absolute path to flutter_assets}"
IVI_HOMESCREEN_APP_OVERRIDE ??= ""
# parameters to pass to homescreen app and Dart VM
SERVICE_EXEC_START_PARAMS ??= ""

SERVICE_UNIT        ??= "Requires=weston@root.service\nAfter=weston@root.service\n"
SERVICE_USER_GROUP  ??= "User=weston\nGroup=weston"
SERVICE_RESTART     ??= "Restart=on-failure\nRestartSec=1"
SERVICE_ENVIRONMENT ??= "Environment=HOME=/home/weston\nEnvironment=XDG_RUNTIME_DIR=/run/user/1000"
SERVICE_EXEC_START  ??= "ExecStart=/usr/bin/homescreen --f ${IVI_HOMESCREEN_APP_OVERRIDE} ${SERVICE_EXEC_START_PARAMS}"
SERVICE_SERVICE     ??= "${SERVICE_USER_GROUP}\n${SERVICE_RESTART}\n${SERVICE_ENVIRONMENT}\n${SERVICE_EXEC_START}\n"
SERVICE_INSTALL     ??= "WantedBy=graphical.target\n"

do_install:append() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
        install -m 644 ${WORKDIR}/homescreen.service.in ${WORKDIR}/homescreen.service
        sed -i "s|@ServiceUnit@|${SERVICE_UNIT}|g" ${WORKDIR}/homescreen.service
        sed -i "s|@ServiceService@|${SERVICE_SERVICE}|g" ${WORKDIR}/homescreen.service
        sed -i "s|@ServiceInstall@|${SERVICE_INSTALL}|g" ${WORKDIR}/homescreen.service
        install -d ${D}${systemd_system_unitdir}
        install -m 644 ${WORKDIR}/homescreen.service ${D}${systemd_system_unitdir}/homescreen.service
    fi
}

SYSTEMD_SERVICE:${PN} += "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'homescreen.service', '', d)}"
SYSTEMD_PACKAGES = "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', '${PN}', '', d)}"


BBCLASSEXTEND = "runtimerelease runtimeprofile runtimedebug"
RDEPENDS:${PN} += "flutter-engine-${@gn_get_flutter_runtime_name(d)}"
