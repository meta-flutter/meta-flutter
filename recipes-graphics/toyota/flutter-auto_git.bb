#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Flutter Auto"
DESCRIPTION = "Toyota's Flutter Auto that communicates with AGL-compositor/Wayland compositors"
AUTHOR = "joel.winarske@toyotaconnected.com"
HOMEPAGE = "https://github.com/toyota-connected/ivi-homescreen"
BUGTRACKER = "https://github.com/toyota-connected/ivi-homescreen/issues"
SECTION = "graphics"
CVE_PRODUCT = "flutter-auto"

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

RDEPENDS:${PN} += "\
    flutter-engine \
"

REQUIRED_DISTRO_FEATURES = "wayland opengl"

SRCREV ??= "98c82158c5ad48220393b39e3cb8206b08166f99"
SRC_URI = "git://github.com/toyota-connected/ivi-homescreen.git;protocol=https;branch=agl"

S = "${WORKDIR}/git"

inherit cmake features_check pkgconfig

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"

PACKAGECONFIG ??= "\
    ${@bb.utils.contains('DISTRO_FEATURES', 'vulkan', 'backend-vulkan', 'backend-egl', d)} \
    transparency \
    isolate \
    restoration \
    platform \
    mouse-cursor \
    text-input \
    key-event \
    url-launcher \
    package-info \
    comp-surf \
    comp-region \
    opengl-texture \
    navigation \
    accessibility \
    desktop-window \
    secure-storage \
    texture-test-egl \
    texture-navi-render-egl \
    "

PACKAGECONFIG[backend-egl] = "-DBUILD_BACKEND_WAYLAND_EGL=ON -DBUILD_BACKEND_WAYLAND_VULKAN=OFF"
PACKAGECONFIG[backend-vulkan] = "-DBUILD_BACKEND_WAYLAND_VULKAN=ON -DBUILD_BACKEND_WAYLAND_EGL=OFF"
PACKAGECONFIG[transparency] = "-DBUILD_EGL_TRANSPARENCY=ON, -DBUILD_EGL_TRANSPARENCY=OFF"
PACKAGECONFIG[isolate] = "-DBUILD_PLUGIN_ISOLATE=ON, -DBUILD_PLUGIN_ISOLATE=OFF"
PACKAGECONFIG[restoration] = "-DBUILD_PLUGIN_RESTORATION=ON, -DBUILD_PLUGIN_RESTORATION=OFF"
PACKAGECONFIG[platform] = "-DBUILD_PLUGIN_PLATFORM=ON, -DBUILD_PLUGIN_PLATFORM=OFF"
PACKAGECONFIG[mouse-cursor] = "-DBUILD_PLUGIN_MOUSE_CURSOR=ON, -DBUILD_PLUGIN_MOUSE_CURSOR=OFF"
PACKAGECONFIG[gstreamer] = "-DBUILD_PLUGIN_GSTREAMER_EGL=ON, -DBUILD_PLUGIN_GSTREAMER_EGL=OFF, gstreamer1.0 gstreamer1.0-plugins-base ffmpeg"
PACKAGECONFIG[text-input] = "-DBUILD_PLUGIN_TEXT_INPUT=ON, -DBUILD_PLUGIN_TEXT_INPUT=OFF"
PACKAGECONFIG[key-event] = "-DBUILD_PLUGIN_KEY_EVENT=ON, -DBUILD_PLUGIN_KEY_EVENT=OFF"
PACKAGECONFIG[url-launcher] = "-DBUILD_PLUGIN_URL_LAUNCHER=ON, -DBUILD_PLUGIN_URL_LAUNCHER=OFF"
PACKAGECONFIG[package-info] = "-DBUILD_PLUGIN_PACKAGE_INFO=ON, -DBUILD_PLUGIN_PACKAGE_INFO=OFF"
PACKAGECONFIG[comp-surf] = "-DBUILD_PLUGIN_COMP_SURF=ON, -DBUILD_PLUGIN_COMP_SURF=OFF"
PACKAGECONFIG[comp-region] = "-DBUILD_PLUGIN_COMP_REGION=ON, -DBUILD_PLUGIN_COMP_REGION=OFF"
PACKAGECONFIG[opengl-texture] = "-DBUILD_PLUGIN_OPENGL_TEXTURE=ON, -DBUILD_PLUGIN_OPENGL_TEXTURE=OFF"
PACKAGECONFIG[navigation] = "-DBUILD_PLUGIN_NAVIGATION=ON, -DBUILD_PLUGIN_NAVIGATION=OFF"
PACKAGECONFIG[accessibility] = "-DBUILD_PLUGIN_ACCESSIBILITY=ON, -DBUILD_PLUGIN_ACCESSIBILITY=OFF"
PACKAGECONFIG[platform-view] = "-DBUILD_PLUGIN_PLATFORM_VIEW=ON, -DBUILD_PLUGIN_PLATFORM_VIEW=OFF"
PACKAGECONFIG[desktop-window] = "-DBUILD_PLUGIN_DESKTOP_WINDOW=ON, -DBUILD_PLUGIN_DESKTOP_WINDOW=OFF"
PACKAGECONFIG[secure-storage] = "-DBUILD_PLUGIN_SECURE_STORAGE=ON, -DBUILD_PLUGIN_SECURE_STORAGE=OFF, libsecret"
PACKAGECONFIG[texture-test-egl] = "-DBUILD_TEXTURE_TEST_EGL=ON, -DBUILD_TEXTURE_TEST_EGL=OFF"
PACKAGECONFIG[texture-navi-render-egl] = "-DBUILD_TEXTURE_NAVI_RENDER_EGL=ON, -DBUILD_TEXTURE_NAVI_RENDER_EGL=OFF"
PACKAGECONFIG[verbose] = "-DCMAKE_BUILD_TYPE=Debug"

EXTRA_OECMAKE += " -D CMAKE_SYSROOT=${STAGING_DIR_TARGET}/usr"

cmake_do_install:append() {
    rm -rf ${D}${libdir}
}

BBCLASSEXTEND = "with-logging"

RDEPENDS:${PN} = "flutter-engine"
