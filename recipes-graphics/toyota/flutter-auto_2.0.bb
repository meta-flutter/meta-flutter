#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Toyota IVI Homescreen v2.0"
DESCRIPTION = "Toyota's Flutter Embedder that communicates with AGL-compositor/Wayland compositors"
AUTHOR = "joel.winarske@toyotaconnected.com"
HOMEPAGE = "https://github.com/toyota-connected/ivi-homescreen"
BUGTRACKER = "https://github.com/toyota-connected/ivi-homescreen/issues"
SECTION = "graphics"

LICENSE = "Apache-2.0 & Apache-2.0"
LIC_FILES_CHKSUM = "\
    file://LICENSE;md5=39ae29158ce710399736340c60147314 \
    file://${S}/ivi-homescreen-plugins/LICENSE;md5=39ae29158ce710399736340c60147314 \
    "

DEPENDS += "\
    glib-2.0 \
    libxkbcommon \
    wayland \
    wayland-native \
    wayland-protocols \
    "

REQUIRED_DISTRO_FEATURES = "wayland"

HOMESCREEN_COMMIT ??= "10a9cdf27d862173c3ff5f85cf67663421f905ec"
PLUGINS_COMMIT ??= "77018f5d9bfca3283115ec8f0655bfa43a2cd991"

SRC_URI = "\
    gitsm://github.com/toyota-connected/ivi-homescreen.git;protocol=https;branch=v2.0;name=homescreen \
    git://github.com/toyota-connected/ivi-homescreen-plugins.git;protocol=https;branch=v2.0;name=plugins;destsuffix=${S}/ivi-homescreen-plugins \
"
SRCREV_FORMAT .= "_homescreen"
SRCREV_homescreen = "${HOMESCREEN_COMMIT}"
SRCREV_FORMAT .= "_plugins"
SRCREV_plugins = "${PLUGINS_COMMIT}"

CRASH_HANDLER_DSN ??= ""

inherit cmake features_check pkgconfig

PACKAGECONFIG ??= "\
    backend-wayland-egl \
    egl-3d \
    egl-transparency \
    egl-multisample \
    \
    client-xdg \
    client-agl-shell \
    \
    ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'wdt_systemd', '', d)} \
    \
    nav_render_view \
    \
    audioplayer_linux \
    ${@bb.utils.contains('IMAGE_INSTALL', 'flatpak', 'flatpak', '', d)} \
    go_router \
    secure-storage \
    url_launcher \
    desktop_window_linux \
    rive-text \
    "

PACKAGECONFIG[backend-wayland-drm] = "-DBUILD_BACKEND_WAYLAND_DRM=ON,-DBUILD_BACKEND_WAYLAND_DRM=OFF"
PACKAGECONFIG[backend-wayland-egl] = "-DBUILD_BACKEND_WAYLAND_EGL=ON,-DBUILD_BACKEND_WAYLAND_EGL=OFF,virtual/egl"
PACKAGECONFIG[backend-wayland-vulkan] = "-DBUILD_BACKEND_WAYLAND_VULKAN=ON,-DBUILD_BACKEND_WAYLAND_VULKAN=OFF,vulkan-loader"

PACKAGECONFIG[client-xdg] = "-DENABLE_XDG_CLIENT=ON,-DENABLE_XDG_CLIENT=OFF"
PACKAGECONFIG[client-agl-shell] = "-DENABLE_AGL_SHELL_CLIENT=ON,-DENABLE_AGL_SHELL_CLIENT=OFF"
PACKAGECONFIG[client-ivi-shell] = "-DENABLE_IVI_SHELL_CLIENT=ON,-DENABLE_IVI_SHELL_CLIENT=OFF"

PACKAGECONFIG[egl-3d] = "-DBUILD_EGL_ENABLE_3D=ON, -DBUILD_EGL_ENABLE_3D=OFF"
PACKAGECONFIG[egl-transparency] = "-DBUILD_EGL_TRANSPARENCY=ON,-DBUILD_EGL_TRANSPARENCY=OFF"
PACKAGECONFIG[egl-multisample] = "-DBUILD_EGL_ENABLE_MULTISAMPLE=ON,-DBUILD_EGL_ENABLE_MULTISAMPLE=OFF"

PACKAGECONFIG[wdt] = "-DBUILD_WATCHDOG=ON, -DBUILD_WATCHDOG=OFF"
PACKAGECONFIG[wdt_systemd] = "-DBUILD_WATCHDOG=ON -DBUILD_SYSTEMD_WATCHDOG=ON, -DBUILD_SYSTEMD_WATCHDOG=OFF, systemd"

PACKAGECONFIG[disable-plugins] = "-DDISABLE_PLUGINS=ON"

PACKAGECONFIG[filament-view] = "\
    -DBUILD_PLUGIN_FILAMENT_VIEW=ON \
    -DFILAMENT_INCLUDE_DIR=${STAGING_INCDIR}/filament \
    -DFILAMENT_LINK_LIBRARIES_DIR=${STAGING_LIBDIR}/filament, \
    -DBUILD_PLUGIN_FILAMENT_VIEW=OFF, filament-vk curl vulkan-loader"
PACKAGECONFIG[layer-playground-view] = "\
    -DBUILD_PLUGIN_LAYER_PLAYGROUND_VIEW=ON, \
    -DBUILD_PLUGIN_LAYER_PLAYGROUND_VIEW=OFF"
PACKAGECONFIG[nav_render_view] = "\
    -DBUILD_PLUGIN_NAV_RENDER_VIEW=ON, \
    -DBUILD_PLUGIN_NAV_RENDER_VIEW=OFF,,"
PACKAGECONFIG[webview_flutter_view] = "\
    -DBUILD_PLUGIN_WEBVIEW_FLUTTER_VIEW=ON, \
    -DBUILD_PLUGIN_WEBVIEW_FLUTTER_VIEW=OFF"

PACKAGECONFIG[audioplayer_linux] = "\
    -DBUILD_PLUGIN_AUDIOPLAYERS_LINUX=ON, \
    -DBUILD_PLUGIN_AUDIOPLAYERS_LINUX=OFF, \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
    "
PACKAGECONFIG[url_launcher] = "-DBUILD_PLUGIN_URL_LAUNCHER=ON,-DBUILD_PLUGIN_URL_LAUNCHER=OFF"
PACKAGECONFIG[secure-storage] = "-DBUILD_PLUGIN_SECURE_STORAGE=ON,-DBUILD_PLUGIN_SECURE_STORAGE=OFF, libsecret"
PACKAGECONFIG[file_selector] = "-DBUILD_PLUGIN_FILE_SELECTOR=ON,-DBUILD_PLUGIN_FILE_SELECTOR=OFF, zenity"
PACKAGECONFIG[cloud_firestore] = "\
    -DBUILD_PLUGIN_CLOUD_FIRESTORE=ON \
    -DFIREBASE_CPP_SDK_DIR=${STAGING_INCDIR}/firebase-cpp-sdk \
    -DFIREBASE_SDK_LIBDIR=${STAGING_LIBDIR}/firebase-cpp-sdk, \
    -DBUILD_PLUGIN_CLOUD_FIRESTORE=OFF \
    "
PACKAGECONFIG[firebase_auth] = "\
    -DBUILD_PLUGIN_FIREBASE_AUTH=ON \
    -DFIREBASE_CPP_SDK_DIR=${STAGING_INCDIR}/firebase-cpp-sdk \
    -DFIREBASE_SDK_LIBDIR=${STAGING_LIBDIR}/firebase-cpp-sdk, \
    -DBUILD_PLUGIN_FIREBASE_AUTH=OFF, \
    libsecret \
"
PACKAGECONFIG[firebase_storage] = "\
    -DBUILD_PLUGIN_FIREBASE_STORAGE=ON \
    -DFIREBASE_CPP_SDK_DIR=${STAGING_INCDIR}/firebase-cpp-sdk \
    -DFIREBASE_SDK_LIBDIR=${STAGING_LIBDIR}/firebase-cpp-sdk, \
    -DBUILD_PLUGIN_FIREBASE_STORAGE=OFF"
PACKAGECONFIG[desktop_window_linux] = "-DBUILD_PLUGIN_DESKTOP_WINDOW_LINUX=ON,-DBUILD_PLUGIN_DESKTOP_WINDOW_LINUX=OFF"
PACKAGECONFIG[flatpak] = "-DBUILD_PLUGIN_FLATPAK=ON, -DBUILD_PLUGIN_FLATPAK=OFF, flatpak"
PACKAGECONFIG[go_router] = "-DBUILD_PLUGIN_GO_ROUTER=ON,-DBUILD_PLUGIN_GO_ROUTER=OFF"
PACKAGECONFIG[google_sign_in] = "-DBUILD_PLUGIN_GOOGLE_SIGN_IN=ON,-DBUILD_PLUGIN_GOOGLE_SIGN_IN=OFF, curl"
PACKAGECONFIG[pdf] = "-DBUILD_PLUGIN_PDF=ON, -DBUILD_PLUGIN_PDF=OFF, pdfium"
PACKAGECONFIG[video_player_linux] = "-DBUILD_PLUGIN_VIDEO_PLAYER_LINUX=ON,-DBUILD_PLUGIN_VIDEO_PLAYER_LINUX=OFF, gstreamer1.0 gstreamer1.0-plugins-base ffmpeg"
PACKAGECONFIG[camera] = "-DBUILD_PLUGIN_CAMERA=ON, -DBUILD_PLUGIN_CAMERA=OFF, libcamera"
PACKAGECONFIG[rive-text] = "-DBUILD_PLUGIN_RIVE_TEXT=ON, -DBUILD_PLUGIN_RIVE_TEXT=OFF, rive-text"

PACKAGECONFIG[sentry] = "\
    -DBUILD_CRASH_HANDLER=ON \
    -DSENTRY_NATIVE_LIBDIR=${STAGING_LIBDIR} \
    -DCRASH_HANDLER_DSN=${CRASH_HANDLER_DSN}, \
    -DBUILD_CRASH_HANDLER=OFF, sentry libunwind"
PACKAGECONFIG[dlt] = "-DENABLE_DLT=ON, -DENABLE_DLT=OFF"
PACKAGECONFIG[sanitize] = "-DSANITIZE_ADDRESS=ON, -DSANITIZE_ADDRESS=OFF"

PACKAGECONFIG[examples] = "-DBUILD_EXAMPLES=ON, -DBUILD_EXAMPLES=OFF"
PACKAGECONFIG[verbose] = "-DCMAKE_BUILD_TYPE=Debug -DDEBUG_PLATFORM_MESSAGES=ON, -DDEBUG_PLATFORM_MESSAGES=OFF"

EXTRA_OECMAKE += "\
    -D PLUGINS_DIR=${S}/ivi-homescreen-plugins/plugins \
    -D LLVM_CONFIG=${STAGING_BINDIR_NATIVE}/llvm-config \
    -D ENABLE_STATIC_LINK=OFF \
    -D ENABLE_LTO=ON \
    -D EXE_OUTPUT_NAME=${PN} \
    -D BUILD_UNIT_TESTS=OFF \
    -D BUILD_DOCS=OFF \
"

RDEPENDS:${PN} += "\
   flutter-engine \
    ${@bb.utils.contains('PACKAGECONFIG', 'rive-text', 'rive-text', '', d)} \
   "

BBCLASSEXTEND = "verbose-logs"
