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
    compiler-rt \
    libcxx \
    libxkbcommon \
    virtual/egl \
    wayland \
    wayland-native \
    wayland-protocols \
    "

REQUIRED_DISTRO_FEATURES = "wayland opengl"

SRCREV ??= "c3abadee678da430203bc23b2a11c7718d857ad5"
SRC_URI = "git://github.com/toyota-connected/ivi-homescreen.git;protocol=https;branch=main"

S = "${WORKDIR}/git"

inherit cmake features_check pkgconfig 

require conf/include/flutter-runtime.inc

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER:libgcc = "compiler-rt"

PACKAGECONFIG ??= "\
    ${@bb.utils.contains('DISTRO_FEATURES', 'vulkan', 'backend-vulkan', 'backend-egl', d)} \
    gstreamer \
    mouse-cursor \
    package-info \
    secure-storage \
    text-input \
    texture-test-egl \
    transparency \
    url-launcher \
    "

PACKAGECONFIG[accessibility] = "-DBUILD_PLUGIN_ACCESSIBILITY=ON, -DBUILD_PLUGIN_ACCESSIBILITY=OFF"
PACKAGECONFIG[backend-egl] = "-DBUILD_BACKEND_WAYLAND_EGL=ON -DBUILD_BACKEND_WAYLAND_VULKAN=OFF"
PACKAGECONFIG[backend-vulkan] = "-DBUILD_BACKEND_WAYLAND_VULKAN=ON -DBUILD_BACKEND_WAYLAND_EGL=OFF"
PACKAGECONFIG[gstreamer] = "-DBUILD_PLUGIN_GSTREAMER_EGL=ON, -DBUILD_PLUGIN_GSTREAMER_EGL=OFF, gstreamer1.0 gstreamer1.0-plugins-base ffmpeg"
PACKAGECONFIG[isolate] = "-DBUILD_PLUGIN_ISOLATE=ON, -DBUILD_PLUGIN_ISOLATE=OFF"
PACKAGECONFIG[mouse-cursor] = "-DBUILD_PLUGIN_MOUSE_CURSOR=ON, -DBUILD_PLUGIN_MOUSE_CURSOR=OFF"
PACKAGECONFIG[navigation] = "-DBUILD_PLUGIN_NAVIGATION=ON, -DBUILD_PLUGIN_NAVIGATION=OFF"
PACKAGECONFIG[package-info] = "-DBUILD_PLUGIN_PACKAGE_INFO=ON, -DBUILD_PLUGIN_PACKAGE_INFO=OFF"
PACKAGECONFIG[platform] = "-DBUILD_PLUGIN_PLATFORM=ON, -DBUILD_PLUGIN_PLATFORM=OFF"
PACKAGECONFIG[platform-view] = "-DBUILD_PLUGIN_PLATFORM_VIEW=ON, -DBUILD_PLUGIN_PLATFORM_VIEW=OFF"
PACKAGECONFIG[restoration] = "-DBUILD_PLUGIN_RESTORATION=ON, -DBUILD_PLUGIN_RESTORATION=OFF"
PACKAGECONFIG[secure-storage] = "-DBUILD_PLUGIN_SECURE_STORAGE=ON, -DBUILD_PLUGIN_SECURE_STORAGE=OFF, libsecret"
PACKAGECONFIG[text-input] = "-DBUILD_PLUGIN_TEXT_INPUT=ON, -DBUILD_PLUGIN_TEXT_INPUT=OFF"
PACKAGECONFIG[texture-test-egl] = "-DBUILD_TEXTURE_TEST_EGL=ON, -DBUILD_TEXTURE_TEST_EGL=OFF"
PACKAGECONFIG[transparency] = "-DBUILD_EGL_TRANSPARENCY=ON, -DBUILD_EGL_TRANSPARENCY=OFF"
PACKAGECONFIG[url-launcher] = "-DBUILD_PLUGIN_URL_LAUNCHER=ON, -DBUILD_PLUGIN_URL_LAUNCHER=OFF"
PACKAGECONFIG[verbose] = "-DCMAKE_BUILD_TYPE=Debug"

# Enable verbose logging on runtimedebug image
PACKAGECONFIG:append:runtimedebug = "verbose"


EXTRA_OECMAKE += " -D CMAKE_SYSROOT=${STAGING_DIR_TARGET}/usr"

cmake_do_install:append() {
    rm -rf ${D}${libdir}
}

BBCLASSEXTEND = "runtimerelease runtimeprofile runtimedebug"
RDEPENDS:${PN} += " flutter-engine-${@gn_get_flutter_runtime_name(d)}"
