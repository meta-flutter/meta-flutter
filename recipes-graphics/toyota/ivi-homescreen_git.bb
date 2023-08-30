#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Toyota IVI Homescreen"
DESCRIPTION = "Toyota's Flutter Embedder that communicates with AGL-compositor/Wayland compositors \
               Quarterly Release"
AUTHOR = "joel.winarske@toyotaconnected.com"
HOMEPAGE = "https://github.com/toyota-connected/ivi-homescreen"
BUGTRACKER = "https://github.com/toyota-connected/ivi-homescreen/issues"
SECTION = "graphics"
CVE_PRODUCT = "homescreen"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=39ae29158ce710399736340c60147314"

DEPENDS += "\
    libxkbcommon \
    virtual/egl \
    wayland \
    wayland-native \
    wayland-protocols \
    "

RDEPENDS_${PN} += "\
    flutter-engine \
"

REQUIRED_DISTRO_FEATURES = "wayland opengl"

SRCREV ??= "0ab7721744cfe4b72b5d0765a90f7e5bff363198"
SRC_URI = "git://github.com/toyota-connected/ivi-homescreen.git;protocol=https;branch=main"

S = "${WORKDIR}/git"

inherit cmake features_check pkgconfig 

PACKAGECONFIG ??= "\
    client-xdg \
    dart-vm-logging \
    ${@bb.utils.contains('DISTRO_FEATURES', 'vulkan', 'backend-vulkan', 'backend-egl', d)} \
    transparency \
    pc-isolate \
    pc-restoration \
    pc-platform \
    pc-mouse-cursor \
    pc-text-input \
    pc-key-event \
    pc-url-launcher \
    pc-package-info \
    pc-comp-surf \
    pc-comp-region \
    pc-opengl-texture \
    pc-navigation \
    pc-accessibility \
    pc-desktop-window \
    pc-logging \
    texture-test-egl \
    texture-navi-render-egl \
    "

PACKAGECONFIG[backend-egl] = "-DBUILD_BACKEND_WAYLAND_EGL=ON -DBUILD_BACKEND_WAYLAND_VULKAN=OFF"
PACKAGECONFIG[backend-vulkan] = "-DBUILD_BACKEND_WAYLAND_VULKAN=ON -DBUILD_BACKEND_WAYLAND_EGL=OFF"
PACKAGECONFIG[backend-drm] = "-DBUILD_BACKEND_WAYLAND_DRM=ON -DBUILD_BACKEND_WAYLAND_DRM=OFF"
PACKAGECONFIG[client-agl] = "-DENABLE_AGL_CLIENT=ON, -DENABLE_AGL_CLIENT=OFF"
PACKAGECONFIG[client-ivi-shell] = "-DENABLE_IVI_SHELL_CLIENT=ON, -DENABLE_IVI_SHELL_CLIENT=OFF"
PACKAGECONFIG[client-xdg] = "-DENABLE_XDG_CLIENT=ON, -DENABLE_XDG_CLIENT=OFF"
PACKAGECONFIG[pc-isolate] = "-DBUILD_PLUGIN_ISOLATE=ON, -DBUILD_PLUGIN_ISOLATE=OFF"
PACKAGECONFIG[pc-restoration] = "-DBUILD_PLUGIN_RESTORATION=ON, -DBUILD_PLUGIN_RESTORATION=OFF"
PACKAGECONFIG[pc-platform] = "-DBUILD_PLUGIN_PLATFORM=ON, -DBUILD_PLUGIN_PLATFORM=OFF"
PACKAGECONFIG[pc-mouse-cursor] = "-DBUILD_PLUGIN_MOUSE_CURSOR=ON, -DBUILD_PLUGIN_MOUSE_CURSOR=OFF"
PACKAGECONFIG[pc-gstreamer] = "-DBUILD_PLUGIN_GSTREAMER_EGL=ON, -DBUILD_PLUGIN_GSTREAMER_EGL=OFF, gstreamer1.0 gstreamer1.0-plugins-base ffmpeg"
PACKAGECONFIG[pc-text-input] = "-DBUILD_PLUGIN_TEXT_INPUT=ON, -DBUILD_PLUGIN_TEXT_INPUT=OFF"
PACKAGECONFIG[pc-key-event] = "-DBUILD_PLUGIN_KEY_EVENT=ON, -DBUILD_PLUGIN_KEY_EVENT=OFF"
PACKAGECONFIG[pc-url-launcher] = "-DBUILD_PLUGIN_URL_LAUNCHER=ON, -DBUILD_PLUGIN_URL_LAUNCHER=OFF"
PACKAGECONFIG[pc-package-info] = "-DBUILD_PLUGIN_PACKAGE_INFO=ON, -DBUILD_PLUGIN_PACKAGE_INFO=OFF"
PACKAGECONFIG[pc-comp-surf] = "-DBUILD_PLUGIN_COMP_SURF=ON, -DBUILD_PLUGIN_COMP_SURF=OFF"
PACKAGECONFIG[pc-comp-region] = "-DBUILD_PLUGIN_COMP_REGION=ON, -DBUILD_PLUGIN_COMP_REGION=OFF"
PACKAGECONFIG[pc-opengl-texture] = "-DBUILD_PLUGIN_OPENGL_TEXTURE=ON, -DBUILD_PLUGIN_OPENGL_TEXTURE=OFF"
PACKAGECONFIG[pc-navigation] = "-DBUILD_PLUGIN_NAVIGATION=ON, -DBUILD_PLUGIN_NAVIGATION=OFF"
PACKAGECONFIG[pc-accessibility] = "-DBUILD_PLUGIN_ACCESSIBILITY=ON, -DBUILD_PLUGIN_ACCESSIBILITY=OFF"
PACKAGECONFIG[pc-platform-view] = "-DBUILD_PLUGIN_PLATFORM_VIEW=ON, -DBUILD_PLUGIN_PLATFORM_VIEW=OFF"
PACKAGECONFIG[pc-desktop-window] = "-DBUILD_PLUGIN_DESKTOP_WINDOW=ON, -DBUILD_PLUGIN_DESKTOP_WINDOW=OFF"
PACKAGECONFIG[pc-secure-storage] = "-DBUILD_PLUGIN_SECURE_STORAGE=ON, -DBUILD_PLUGIN_SECURE_STORAGE=OFF, libsecret"
PACKAGECONFIG[pc-integration-test] = "-DBUILD_PLUGIN_INTEGRATION_TEST=ON, -DBUILD_PLUGIN_INTEGRATION_TEST=OFF"
PACKAGECONFIG[pc-logging] = "-DBUILD_PLUGIN_LOGGING=ON, -DBUILD_PLUGIN_LOGGING=OFF"
PACKAGECONFIG[texture-test-egl] = "-DBUILD_TEXTURE_TEST_EGL=ON, -DBUILD_TEXTURE_TEST_EGL=OFF"
PACKAGECONFIG[texture-navi-render-egl] = "-DBUILD_TEXTURE_NAVI_RENDER_EGL=ON, -DBUILD_TEXTURE_NAVI_RENDER_EGL=OFF"

PACKAGECONFIG[dart-vm-logging] = "-DENABLE_DART_VM_LOGGING=ON, -DENABLE_DART_VM_LOGGING=OFF"
PACKAGECONFIG[dlt] = "-DENABLE_DLT=ON, -DENABLE_DLT=OFF"
PACKAGECONFIG[sentry-native] = "-DBUILD_CRASH_HANDLER=ON, -DBUILD_CRASH_HANDLER=OFF, sentry-native"
PACKAGECONFIG[transparency] = "-DBUILD_EGL_TRANSPARENCY=ON, -DBUILD_EGL_TRANSPARENCY=OFF"

PACKAGECONFIG[verbose] = "-DCMAKE_BUILD_TYPE=Debug"


EXTRA_OECMAKE += " -D CMAKE_SYSROOT=${STAGING_DIR_TARGET}/usr"

cmake_do_install_append() {
    rm -rf ${D}${libdir}
}

BBCLASSEXTEND = "with-logging"

RDEPENDS_${PN} = "flutter-engine"
