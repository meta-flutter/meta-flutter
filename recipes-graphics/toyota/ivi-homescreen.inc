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

PACKAGECONFIG ??= "gles3 context_version_3 transparency ${@bb.utils.filter('DISTRO_FEATURES', 'systemd', d)}"

PACKAGECONFIG[gles3] = "-DBUILD_EGL_OPENGL_ES3=ON, -DBUILD_EGL_OPENGL_ES3=OFF"
PACKAGECONFIG[gles2] = "-DBUILD_EGL_OPENGL_ES2=ON, -DBUILD_EGL_OPENGL_ES2=OFF"
PACKAGECONFIG[context_version_3] = "-DBUILD_EGL_CONTEXT_VERSION_MAJOR=3"
PACKAGECONFIG[context_version_2] = "-DBUILD_EGL_CONTEXT_VERSION_MAJOR=2"
PACKAGECONFIG[context_version_1] = "-DBUILD_EGL_CONTEXT_VERSION_MAJOR=1"
PACKAGECONFIG[transparency] = "-DBUILD_EGL_TRANSPARENCY=ON, -DBUILD_EGL_TRANSPARENCY=OFF"

EXTRA_OECMAKE += "-D CMAKE_SYSROOT=${STAGING_DIR_TARGET}/usr"

FLUTTER_RUNTIME ??= "release"

# Use pattern "--a={absolute path to flutter_assets}"
IVI_HOMESCREEN_APP_OVERRIDE ??= ""
# parameters to pass to homescreen app and Dart VM
FLUTTER_HOMESCREEN_SERVICE_EXEC_START_PARAMS ??= ""

FLUTTER_HOMESCREEN_SERVICE_UNIT        ??= "Requires=weston@root.service\nAfter=weston@root.service\n"
FLUTTER_HOMESCREEN_SERVICE_USER_GROUP  ??= "User=root\nGroup=root"
FLUTTER_HOMESCREEN_SERVICE_RESTART     ??= "Restart=on-failure\nRestartSec=1"
FLUTTER_HOMESCREEN_SERVICE_ENVIRONMENT ??= "Environment=HOME=/home/root\nEnvironment=XDG_RUNTIME_DIR=/run/user/0"
FLUTTER_HOMESCREEN_SERVICE_EXEC_START  ??= "ExecStart=/usr/bin/homescreen --f ${IVI_HOMESCREEN_APP_OVERRIDE} ${FLUTTER_HOMESCREEN_SERVICE_EXEC_START_PARAMS}"
FLUTTER_HOMESCREEN_SERVICE_SERVICE     ??= "${FLUTTER_HOMESCREEN_SERVICE_USER_GROUP}\n${FLUTTER_HOMESCREEN_SERVICE_RESTART}\n${FLUTTER_HOMESCREEN_SERVICE_ENVIRONMENT}\n${FLUTTER_HOMESCREEN_SERVICE_EXEC_START}\n"
FLUTTER_HOMESCREEN_SERVICE_INSTALL     ??= "WantedBy=graphical.target\n"

do_install_append() {
    if ${@bb.utils.contains('PACKAGECONFIG', 'systemd', 'true', 'false', d)}; then
        install -m 644 ${WORKDIR}/homescreen.service.in ${WORKDIR}/homescreen.service
        sed -i "s|@ServiceUnit@|${FLUTTER_HOMESCREEN_SERVICE_UNIT}|g" ${WORKDIR}/homescreen.service
        sed -i "s|@ServiceService@|${FLUTTER_HOMESCREEN_SERVICE_SERVICE}|g" ${WORKDIR}/homescreen.service
        sed -i "s|@ServiceInstall@|${FLUTTER_HOMESCREEN_SERVICE_INSTALL}|g" ${WORKDIR}/homescreen.service
        install -d ${D}${systemd_system_unitdir}
        install -m 644 ${WORKDIR}/homescreen.service ${D}${systemd_system_unitdir}/homescreen.service
    fi
}

SYSTEMD_SERVICE_${PN} += "${@bb.utils.contains('PACKAGECONFIG', 'systemd', 'homescreen.service', '', d)}"
SYSTEMD_PACKAGES = "${@bb.utils.contains('PACKAGECONFIG', 'systemd', '${PN}', '', d)}"
