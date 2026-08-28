#
# Copyright (c) 2020-2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# Shared body for the ivi-homescreen v3.0 embedder recipes. The two consumers
# (ivi-homescreen_3.0.bb, flutter-auto_3.0.bb) differ only in EXE_OUTPUT_NAME,
# their default PACKAGECONFIG, and their runtime dependencies.
#
# v3 highlights vs v2:
#   * Backends are no longer mutually exclusive. Every BUILD_BACKEND_* may be
#     ON together; the active one is resolved at runtime by the shell registry.
#     New backends: DRM/KMS (EGL + Vulkan), software (CPU), Wayland leased-DRM,
#     headless EGL, headless Vulkan.
#   * New subsystems: FlutterCompositor backing stores (+ Dear ImGui HUD),
#     accessibility semantics tree, MCP surface, AccessKit adapter, OSGi
#     multi-bundle framework, coverage-guided fuzz targets.
#   * The Wayland bindings are generated at build time by wayland-cxx-scanner,
#     which must run on the build host -- hence wayland-cxx-scanner-native.
#   * The crash handler DSN now comes from the SENTRY_DSN environment variable
#     at runtime; CRASH_HANDLER_DSN no longer exists.
#   * filament-view was removed from the plugins repo.
#

DESCRIPTION = "Toyota's Flutter Embedder that communicates with AGL-compositor/Wayland compositors"
AUTHOR = "joel.winarske@toyotaconnected.com"
HOMEPAGE = "https://github.com/toyota-connected/ivi-homescreen"
BUGTRACKER = "https://github.com/toyota-connected/ivi-homescreen/issues"
SECTION = "graphics"
CVE_PRODUCT = "homescreen"

# The plugins tree (Apache-2.0) and the v4l2-webrtc-codec encoder sources (MIT)
# are only fetched when they are actually built, so the checksums follow the
# PACKAGECONFIG. The license list only has to follow it for the encoder: the
# plugins tree is Apache-2.0 like the base, and naming it twice is not a second
# license, just a duplicate operand for oe-core to flag.
LICENSE = "Apache-2.0\
${@bb.utils.contains_any('PACKAGECONFIG', 'software-sink-encoder backend-headless-egl', ' AND MIT', '', d)}"

LIC_FILES_CHKSUM = "\
    file://LICENSE;md5=39ae29158ce710399736340c60147314 \
    ${@bb.utils.contains('PACKAGECONFIG', 'disable-plugins', '', 'file://${S}/ivi-homescreen-plugins/LICENSE;md5=39ae29158ce710399736340c60147314', d)} \
    ${@bb.utils.contains_any('PACKAGECONFIG', 'software-sink-encoder backend-headless-egl', 'file://${S}/v4l2-webrtc-codec/LICENSE;md5=8992d37861cd7e48b171be43673f1d7f', '', d)} \
    "

# Unconditional only. libxkbcommon is genuinely always needed: platform_homescreen
# probes it REQUIRED regardless of backend (shell/platform/homescreen/CMakeLists.txt).
# Everything Wayland moved onto the Wayland backends below -- v3 builds DRM/KMS,
# software and headless binaries that link no Wayland at all.
DEPENDS += "\
    glib-2.0 \
    libxkbcommon \
    compiler-rt \
    libcxx \
    lld-native \
"

# The always-on 'common' plugin vendors sdbus-c++, which requires an sd-bus
# implementation at configure time (libsystemd >= 238, libelogind >= 238, or
# basu). systemd only exists as a build dependency when the distro has the
# feature, so fall back to basu (meta-oe) otherwise. Not needed when the plugin
# tree is compiled out entirely.
SDBUS_PROVIDER = "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', 'basu', d)}"
DEPENDS += "${@bb.utils.contains('PACKAGECONFIG', 'disable-plugins', '', d.getVar('SDBUS_PROVIDER'), d)}"

# Only the backends that actually open a display need the distro feature.
REQUIRED_DISTRO_FEATURES = "\
    ${@bb.utils.contains_any('PACKAGECONFIG', 'backend-wayland-egl backend-wayland-vulkan backend-wayland-leased-drm', 'wayland', '', d)} \
    ${@bb.utils.contains_any('PACKAGECONFIG', 'backend-wayland-egl backend-drm-kms-egl backend-headless-egl', 'opengl', '', d)} \
"

HOMESCREEN_COMMIT ??= "131cbc37a1248f394978b408d4c02f1ea7eee6c3"
PLUGINS_COMMIT ??= "aa310172ac93efbfaf2beaaab7d99bb1f8fd586e"
# v4l2-webrtc-codec supplies the V4L2 M2M encoder sources the embedder compiles
# in directly (it installs nothing, so it is consumed as a source tree via
# V4L2WC_DIR rather than as a library).
V4L2WC_COMMIT ??= "08bddc2b63e709c3419e40d6d2ed6599e0f412be"

# The plugins repo has no v3.0 branch; v3.0 builds against the plugins v2.0
# branch, which is what upstream CI and the emb board manifests use.
#
# Both extra trees are fetched only when built: 'disable-plugins' means no
# plugins clone at all (and so no sd-bus dependency), and the encoder sources are
# pulled only by the options that compile them.
PLUGINS_SRC_URI = "gitsm://github.com/toyota-connected/ivi-homescreen-plugins.git;protocol=https;branch=v2.0;name=plugins;destsuffix=${S}/ivi-homescreen-plugins"
V4L2WC_SRC_URI = "git://github.com/jwinarske/v4l2-webrtc-codec.git;protocol=https;branch=main;name=v4l2wc;destsuffix=${S}/v4l2-webrtc-codec"

SRC_URI = "\
    gitsm://github.com/toyota-connected/ivi-homescreen.git;protocol=https;branch=v3.0;name=homescreen \
    ${@bb.utils.contains('PACKAGECONFIG', 'disable-plugins', '', d.getVar('PLUGINS_SRC_URI'), d)} \
    ${@bb.utils.contains_any('PACKAGECONFIG', 'software-sink-encoder backend-headless-egl', d.getVar('V4L2WC_SRC_URI'), '', d)} \
"

SRCREV_FORMAT = "homescreen"
SRCREV_FORMAT .= "${@bb.utils.contains('PACKAGECONFIG', 'disable-plugins', '', '_plugins', d)}"
SRCREV_FORMAT .= "${@bb.utils.contains_any('PACKAGECONFIG', 'software-sink-encoder backend-headless-egl', '_v4l2wc', '', d)}"
SRCREV_homescreen = "${HOMESCREEN_COMMIT}"
SRCREV_plugins = "${PLUGINS_COMMIT}"
SRCREV_v4l2wc = "${V4L2WC_COMMIT}"

#
# Integrator-supplied knobs
#
# The IME protocol is a compositor capability, not a project default: a wrong
# value fails at runtime as dead text input, not at build. GNOME/Mutter and KDE
# speak text-input-v3; AGL/weston stacks speak text-input-v1. Valid values:
#   none text-input-v1 text-input-v3 input-method-v1 input-method-v2
#   virtual-keyboard-v1
WAYLAND_CXX_IME_BACKEND ??= "text-input-v3"

# Absolute path to crashpad_handler on the deployed target (PACKAGECONFIG sentry).
CRASHPAD_RUNTIME_PATH ??= "${bindir}/crashpad_handler"

# External source/SDK trees that a few opt-in features need. None of these has
# an OE recipe today, so the features that consume them stay off unless an
# integrator points these at a staged checkout.
ACCESSKIT_DIR ??= ""
V4L2WC_DIR ??= "${S}/v4l2-webrtc-codec"
CEF_ROOT ??= ""
# tcna-packages checkout that supplies the video_player_linux Dart IDL, for the
# opt-in pigeon regeneration target.
VIDEO_PLAYER_LINUX_PIGEON_DIR ??= ""

# Resolved from the libwebrtc recipe, for the embedder's own headless WebRTC
# send consumer (software-sink-encoder-webrtc). LIBWEBRTC_DIR is a prefix whose
# include/ holds the flat C ABI header. The option that consumes these adds the
# DEPENDS, so these are just paths.
LIBWEBRTC_DIR ??= "${STAGING_DATADIR}/libwebrtc"
LIBWEBRTC_SO ??= "${STAGING_LIBDIR}/libwebrtc.so"

TOOLCHAIN = "clang"
TOOLCHAIN_NATIVE = "clang"
TC_CXX_RUNTIME = "llvm"
PREFERRED_PROVIDER_llvm = "clang"
PREFERRED_PROVIDER_llvm-native = "clang-native"
PREFERRED_PROVIDER_libgcc = "compiler-rt"
LIBCPLUSPLUS = "-stdlib=libc++"

inherit cmake features_check pkgconfig

#
# Backends
#
# Any combination may be enabled; the shell registry picks one at runtime.
# The EGL platform-view path imports dma-bufs, hence libdrm on the EGL backend.
#
# wayland-native supplies wayland-scanner's pkgdatadir (core wayland.xml) and
# wayland-cxx-scanner-native is the build-host code generator: both are needed by
# every backend that opens a wl_display, i.e. IVI_WAYLAND_ANY.
#
# wayland-protocols is deliberately NOT on the leased-DRM backend: a lease-only
# build has no wl_surface and reads nothing from the package -- it takes core
# wayland.xml from wayland-scanner and drm-lease-v1.xml is vendored in-tree.
PACKAGECONFIG[backend-wayland-egl] = "-DBUILD_BACKEND_WAYLAND_EGL=ON,-DBUILD_BACKEND_WAYLAND_EGL=OFF,\
    virtual/egl \
    virtual/libgles2 \
    libdrm \
    wayland \
    wayland-native \
    wayland-protocols \
    wayland-cxx-scanner-native \
    "
PACKAGECONFIG[backend-wayland-vulkan] = "-DBUILD_BACKEND_WAYLAND_VULKAN=ON,-DBUILD_BACKEND_WAYLAND_VULKAN=OFF,\
    libdrm \
    wayland \
    wayland-native \
    wayland-protocols \
    wayland-cxx-scanner-native \
    ,vulkan-loader"
PACKAGECONFIG[backend-wayland-leased-drm] = "-DBUILD_BACKEND_WAYLAND_LEASED_DRM=ON,\
    -DBUILD_BACKEND_WAYLAND_LEASED_DRM=OFF,\
    libdrm \
    wayland \
    wayland-native \
    wayland-cxx-scanner-native \
    "
PACKAGECONFIG[backend-drm-kms-egl] = "-DBUILD_BACKEND_DRM_KMS_EGL=ON,-DBUILD_BACKEND_DRM_KMS_EGL=OFF,\
    libdrm \
    virtual/libgbm \
    virtual/egl \
    virtual/libgles2 \
    libinput \
    udev \
    libdisplay-info \
    "
PACKAGECONFIG[backend-drm-kms-vulkan] = "-DBUILD_BACKEND_DRM_KMS_VULKAN=ON,-DBUILD_BACKEND_DRM_KMS_VULKAN=OFF,\
    libdrm \
    virtual/libgbm \
    libinput \
    udev \
    libdisplay-info \
    ,vulkan-loader"
PACKAGECONFIG[backend-software] = "-DBUILD_BACKEND_SOFTWARE=ON,-DBUILD_BACKEND_SOFTWARE=OFF"
PACKAGECONFIG[backend-headless-egl] = "-DBUILD_BACKEND_HEADLESS_EGL=ON -DV4L2WC_DIR=${V4L2WC_DIR},\
    -DBUILD_BACKEND_HEADLESS_EGL=OFF,virtual/libgbm virtual/egl virtual/libgles2"
PACKAGECONFIG[backend-headless-vulkan] = "-DBUILD_BACKEND_HEADLESS_VULKAN=ON,\
    -DBUILD_BACKEND_HEADLESS_VULKAN=OFF,,vulkan-loader"

# Software-backend output sinks and input seat. The CMake defaults autodetect
# from the sysroot; pin them so the feature set is a property of the recipe.
PACKAGECONFIG[software-sink-drm] = "-DBUILD_SOFTWARE_SINK_DRM=ON,-DBUILD_SOFTWARE_SINK_DRM=OFF,libdrm"
PACKAGECONFIG[software-sink-fbdev] = "-DBUILD_SOFTWARE_SINK_FBDEV=ON,-DBUILD_SOFTWARE_SINK_FBDEV=OFF"
PACKAGECONFIG[software-input-libinput] = "-DBUILD_SOFTWARE_INPUT_LIBINPUT=ON,\
    -DBUILD_SOFTWARE_INPUT_LIBINPUT=OFF,libinput udev libxkbcommon"
PACKAGECONFIG[software-sink-encoder] = "-DBUILD_SOFTWARE_SINK_ENCODER=ON -DV4L2WC_DIR=${V4L2WC_DIR},\
    -DBUILD_SOFTWARE_SINK_ENCODER=OFF"
PACKAGECONFIG[software-sink-encoder-webrtc] = "\
    -DBUILD_SOFTWARE_SINK_ENCODER_WEBRTC=ON \
    -DLIBWEBRTC_DIR=${LIBWEBRTC_DIR} \
    -DLIBWEBRTC_SO=${LIBWEBRTC_SO}, \
    -DBUILD_SOFTWARE_SINK_ENCODER_WEBRTC=OFF, libwebrtc"

# EGL surface configuration (Wayland EGL backend only).
PACKAGECONFIG[egl-3d] = "-DBUILD_EGL_ENABLE_3D=ON,-DBUILD_EGL_ENABLE_3D=OFF"
PACKAGECONFIG[egl-transparency] = "-DBUILD_EGL_TRANSPARENCY=ON,-DBUILD_EGL_TRANSPARENCY=OFF"
PACKAGECONFIG[egl-multisample] = "-DBUILD_EGL_ENABLE_MULTISAMPLE=ON,-DBUILD_EGL_ENABLE_MULTISAMPLE=OFF"

# FlutterCompositor backing-store API: needed for interleaved platform-view
# layers. The Dear ImGui debug HUD requires it and is force-disabled without it.
PACKAGECONFIG[compositor] = "-DBUILD_COMPOSITOR=ON,-DBUILD_COMPOSITOR=OFF"
PACKAGECONFIG[compositor-dmabuf-export] = "-DBUILD_COMPOSITOR_DMABUF_EXPORT=ON,\
    -DBUILD_COMPOSITOR_DMABUF_EXPORT=OFF"
PACKAGECONFIG[hud] = "-DBUILD_HUD=ON,-DBUILD_HUD=OFF"

# Vendor the Khronos validation layer into the Vulkan backend, and build the
# standalone zero-copy scanout capability probe.
PACKAGECONFIG[vulkan-validation] = "-DBUILD_VULKAN_VALIDATION=ON,-DBUILD_VULKAN_VALIDATION=OFF"
PACKAGECONFIG[drm-kms-vulkan-probe] = "-DBUILD_DRM_KMS_VULKAN_PROBE=ON,\
    -DBUILD_DRM_KMS_VULKAN_PROBE=OFF,libdrm"

#
# Wayland shell clients
#
PACKAGECONFIG[client-xdg] = "-DENABLE_XDG_CLIENT=ON,-DENABLE_XDG_CLIENT=OFF"
PACKAGECONFIG[client-agl-shell] = "-DENABLE_AGL_SHELL_CLIENT=ON,-DENABLE_AGL_SHELL_CLIENT=OFF"
PACKAGECONFIG[client-ivi-shell] = "-DENABLE_IVI_SHELL_CLIENT=ON,-DENABLE_IVI_SHELL_CLIENT=OFF"
PACKAGECONFIG[client-simple-shell] = "-DENABLE_SIMPLE_SHELL_CLIENT=ON,-DENABLE_SIMPLE_SHELL_CLIENT=OFF"

#
# Accessibility
#
# Both mcp and accesskit are served from the semantics tree, so each requires
# accessibility to be enabled alongside it. CMake fails the configure with an
# explicit message when it is not, rather than silently building without it.
#
PACKAGECONFIG[accessibility] = "-DBUILD_ACCESSIBILITY=ON,-DBUILD_ACCESSIBILITY=OFF"
PACKAGECONFIG[mcp] = "-DBUILD_MCP=ON,-DBUILD_MCP=OFF"
PACKAGECONFIG[accesskit] = "\
    -DBUILD_ACCESSKIT=ON \
    -DACCESSKIT_DIR=${ACCESSKIT_DIR}, \
    -DBUILD_ACCESSKIT=OFF"

#
# OSGi multi-bundle framework (several Flutter bundles with an OSGi lifecycle).
#
PACKAGECONFIG[osgi] = "-DENABLE_OSGI=ON,-DENABLE_OSGI=OFF"

#
# Watchdog
#
PACKAGECONFIG[wdt] = "-DBUILD_WATCHDOG=ON,-DBUILD_WATCHDOG=OFF"
PACKAGECONFIG[wdt_systemd] = "-DBUILD_WATCHDOG=ON -DBUILD_SYSTEMD_WATCHDOG=ON,\
    -DBUILD_SYSTEMD_WATCHDOG=OFF,systemd"

#
# Diagnostics
#
# The DLT sink dlopens libdlt at runtime, so it adds no build-time dependency.
#
PACKAGECONFIG[sentry] = "\
    -DBUILD_CRASH_HANDLER=ON \
    -DSENTRY_NATIVE_LIBDIR=${STAGING_LIBDIR} \
    -DCRASHPAD_RUNTIME_PATH=${CRASHPAD_RUNTIME_PATH}, \
    -DBUILD_CRASH_HANDLER=OFF, sentry libunwind"
PACKAGECONFIG[dlt] = "-DENABLE_DLT=ON,-DENABLE_DLT=OFF,,dlt-daemon"
PACKAGECONFIG[verbose] = "-DCMAKE_BUILD_TYPE=Debug -DDEBUG_PLATFORM_MESSAGES=ON,\
    -DDEBUG_PLATFORM_MESSAGES=OFF"

# Sanitizers. Do not combine with fuzzers: the fuzz targets carry their own
# -fsanitize list and CMake rejects the combination.
PACKAGECONFIG[sanitize] = "-DSANITIZE_ADDRESS=ON,-DSANITIZE_ADDRESS=OFF"
PACKAGECONFIG[sanitize-thread] = "-DSANITIZE_THREAD=ON,-DSANITIZE_THREAD=OFF"
PACKAGECONFIG[sanitize-memory] = "-DSANITIZE_MEMORY=ON,-DSANITIZE_MEMORY=OFF"
PACKAGECONFIG[sanitize-undefined] = "-DSANITIZE_UNDEFINED=ON,-DSANITIZE_UNDEFINED=OFF"

#
# Build products that are not the shell itself
#
PACKAGECONFIG[unit-tests] = "-DBUILD_UNIT_TESTS=ON,-DBUILD_UNIT_TESTS=OFF"
PACKAGECONFIG[integration-tests] = "-DBUILD_INTEGRATION_TESTS=ON,-DBUILD_INTEGRATION_TESTS=OFF"
PACKAGECONFIG[unit-test-goldens] = "-DUNIT_TEST_SAVE_GOLDENS=ON,-DUNIT_TEST_SAVE_GOLDENS=OFF"
PACKAGECONFIG[fuzzers] = "-DBUILD_FUZZERS=ON,-DBUILD_FUZZERS=OFF"
PACKAGECONFIG[integration-test-crash-handler] = "-DINTEGRATION_TEST_CRASH_HANDLER=ON,\
    -DINTEGRATION_TEST_CRASH_HANDLER=OFF"
PACKAGECONFIG[integration-test-systemd-watchdog] = "-DINTEGRATION_TEST_SYSTEMD_WATCHDOG=ON,\
    -DINTEGRATION_TEST_SYSTEMD_WATCHDOG=OFF"
PACKAGECONFIG[docs] = "-DBUILD_DOCS=ON,-DBUILD_DOCS=OFF,doxygen-native"
PACKAGECONFIG[lto] = "-DENABLE_LTO=ON,-DENABLE_LTO=OFF"
PACKAGECONFIG[static-link] = "-DENABLE_STATIC_LINK=ON,-DENABLE_STATIC_LINK=OFF"

#
# Plugins (ivi-homescreen-plugins)
#
PACKAGECONFIG[disable-plugins] = "-DDISABLE_PLUGINS=ON,-DDISABLE_PLUGINS=OFF"

PACKAGECONFIG[audioplayer_linux] = "\
    -DBUILD_PLUGIN_AUDIOPLAYERS_LINUX=ON, \
    -DBUILD_PLUGIN_AUDIOPLAYERS_LINUX=OFF, \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    gstreamer1.0-plugins-good \
    "
PACKAGECONFIG[camera] = "-DBUILD_PLUGIN_CAMERA=ON,-DBUILD_PLUGIN_CAMERA=OFF,libcamera"
PACKAGECONFIG[camera-pipewire] = "-DBUILD_PLUGIN_CAMERA_PIPEWIRE=ON -DBUILD_PLUGIN_CAMERA=OFF,\
    -DBUILD_PLUGIN_CAMERA_PIPEWIRE=OFF,pipewire jpeg glib-2.0"
PACKAGECONFIG[cloud_firestore] = "\
    -DBUILD_PLUGIN_CLOUD_FIRESTORE=ON \
    -DFIREBASE_CPP_SDK_DIR=${STAGING_INCDIR}/firebase-cpp-sdk \
    -DFIREBASE_SDK_LIBDIR=${STAGING_LIBDIR}/firebase-cpp-sdk, \
    -DBUILD_PLUGIN_CLOUD_FIRESTORE=OFF \
    "
PACKAGECONFIG[desktop_window_linux] = "-DBUILD_PLUGIN_DESKTOP_WINDOW_LINUX=ON,\
    -DBUILD_PLUGIN_DESKTOP_WINDOW_LINUX=OFF"
PACKAGECONFIG[file_selector] = "-DBUILD_PLUGIN_FILE_SELECTOR=ON,-DBUILD_PLUGIN_FILE_SELECTOR=OFF,,zenity"
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
PACKAGECONFIG[flatpak] = "-DBUILD_PLUGIN_FLATPAK=ON,-DBUILD_PLUGIN_FLATPAK=OFF,flatpak libxml2 zlib,flatpak"
PACKAGECONFIG[go_router] = "-DBUILD_PLUGIN_GO_ROUTER=ON,-DBUILD_PLUGIN_GO_ROUTER=OFF"
# The 'common' plugin is upstream-default ON and is what pulls in the vendored
# sdbus-c++ (and so the sd-bus provider dependency). Disabling it is the
# fine-grained alternative to disable-plugins -- but every other plugin links
# plugin_common / plugin_common_glib / plugin_common_curl, so it can only be
# turned off when no other plugin is enabled.
PACKAGECONFIG[plugin-common] = "-DBUILD_PLUGIN_COMMON=ON,-DBUILD_PLUGIN_COMMON=OFF"
# firebase_core is declared by the plugins tree only when a firebase plugin is
# enabled, and defaults ON there. Deliberately no disable value: forcing OFF
# would break those plugins, so absence leaves upstream's own logic in charge.
PACKAGECONFIG[firebase_core] = "-DBUILD_PLUGIN_FIREBASE_CORE=ON,,"
# Developer target that re-runs Pigeon over the video_player_linux Dart IDL.
# Needs dart, clang-format and sed on PATH plus VIDEO_PLAYER_LINUX_PIGEON_DIR.
PACKAGECONFIG[video-player-pigeon-regen] = "\
    -DBUILD_PLUGIN_VIDEO_PLAYER_LINUX_PIGEON_REGEN=ON \
    -DVIDEO_PLAYER_LINUX_PIGEON_DIR=${VIDEO_PLAYER_LINUX_PIGEON_DIR}, \
    -DBUILD_PLUGIN_VIDEO_PLAYER_LINUX_PIGEON_REGEN=OFF"
PACKAGECONFIG[google_sign_in] = "-DBUILD_PLUGIN_GOOGLE_SIGN_IN=ON,-DBUILD_PLUGIN_GOOGLE_SIGN_IN=OFF,curl"
PACKAGECONFIG[integration_test] = "-DBUILD_PLUGIN_INTEGRATION_TEST=ON,-DBUILD_PLUGIN_INTEGRATION_TEST=OFF"
PACKAGECONFIG[layer-playground-view] = "\
    -DBUILD_PLUGIN_LAYER_PLAYGROUND_VIEW=ON, \
    -DBUILD_PLUGIN_LAYER_PLAYGROUND_VIEW=OFF"
PACKAGECONFIG[nav_render_view] = "\
    -DBUILD_PLUGIN_NAV_RENDER_VIEW=ON, \
    -DBUILD_PLUGIN_NAV_RENDER_VIEW=OFF"
PACKAGECONFIG[pdf] = "-DBUILD_PLUGIN_PDF=ON,-DBUILD_PLUGIN_PDF=OFF,pdfium,pdfium"
PACKAGECONFIG[rive-text] = "-DBUILD_PLUGIN_RIVE_TEXT=ON,-DBUILD_PLUGIN_RIVE_TEXT=OFF,rive-text,rive-text"
PACKAGECONFIG[secure-storage] = "-DBUILD_PLUGIN_SECURE_STORAGE=ON,-DBUILD_PLUGIN_SECURE_STORAGE=OFF,libsecret"
PACKAGECONFIG[url_launcher] = "-DBUILD_PLUGIN_URL_LAUNCHER=ON,-DBUILD_PLUGIN_URL_LAUNCHER=OFF"
PACKAGECONFIG[video-player] = "-DBUILD_PLUGIN_VIDEO_PLAYER_LINUX=ON,-DBUILD_PLUGIN_VIDEO_PLAYER_LINUX=OFF, \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    udev \
    virtual/egl \
    virtual/libgles2 \
    glib-2.0 \
    "
# NOTE: the plugins repo's plugins/webrtc is deliberately NOT exposed. It links
# libwebrtc's C++ API directly into the embedder, and its vendored flutter-webrtc
# copy has drifted from current libwebrtc (e.g. the Algorithm ->
# FrameCryptorAlgorithm rename). The supported path is the out-of-tree
# ihs_webrtc_view plugin, which speaks only the flat C ABIs (lw_video_sink.h +
# the ivi-homescreen platform-view ABI) and links neither libwebrtc nor
# libihs_shared, so the embedder itself stays free of a libwebrtc dependency.
# Needs a CEF distribution staged at CEF_ROOT.
PACKAGECONFIG[webview_flutter_view] = "\
    -DBUILD_PLUGIN_WEBVIEW_FLUTTER_VIEW=ON \
    -DCEF_ROOT=${CEF_ROOT}, \
    -DBUILD_PLUGIN_WEBVIEW_FLUTTER_VIEW=OFF"

#
# PACKAGECONFIG consistency matrix
#
# v3 backends are NOT mutually exclusive -- any set may be compiled in and the
# active one is resolved at runtime. What IS constrained is the set of
# combinations upstream's CMake rejects, plus the options that silently do
# nothing without their partner. Upstream raises most of these as
# FATAL_ERROR part-way through do_configure; catching them at parse time turns
# a confused configure log into one line naming the option to change.
#
python () {
    pc = set((d.getVar('PACKAGECONFIG') or '').split())
    pn = d.getVar('PN')

    def need(var):
        v = d.getVar(var)
        return v.strip() if v else ''

    backends = {'backend-wayland-egl', 'backend-wayland-vulkan',
                'backend-wayland-leased-drm', 'backend-drm-kms-egl',
                'backend-drm-kms-vulkan', 'backend-software',
                'backend-headless-egl', 'backend-headless-vulkan'}
    enabled_backends = pc & backends

    # CMakeLists.txt: the registry would be empty and main() would have nothing
    # to run.
    if not enabled_backends:
        bb.fatal("%s: no backend selected. Enable at least one of: %s"
                 % (pn, ' '.join(sorted(backends))))

    # options.cmake: a lease acquires a DRM fd, it does not render, so a
    # renderer tier must be able to scan out on it.
    if 'backend-wayland-leased-drm' in pc:
        tiers = pc & {'backend-drm-kms-egl', 'backend-drm-kms-vulkan'}
        if 'backend-software' in pc and 'software-sink-drm' in pc:
            tiers = tiers | {'backend-software'}
        if not tiers:
            bb.fatal("%s: backend-wayland-leased-drm needs a renderer tier. Add one of: "
                     "backend-drm-kms-egl, backend-drm-kms-vulkan, or "
                     "backend-software + software-sink-drm" % pn)

    # options.cmake: both surfaces are served from the semantics tree.
    for opt in ('mcp', 'accesskit'):
        if opt in pc and 'accessibility' not in pc:
            bb.fatal("%s: PACKAGECONFIG '%s' requires 'accessibility'" % (pn, opt))

    # options.cmake: the fuzz targets carry their own -fsanitize list.
    sanitizers = pc & {'sanitize', 'sanitize-thread', 'sanitize-memory',
                       'sanitize-undefined'}
    if 'fuzzers' in pc and sanitizers:
        bb.fatal("%s: 'fuzzers' conflicts with %s -- two -fsanitize lists on one "
                 "link either fail or silently override each other"
                 % (pn, ', '.join(sorted(sanitizers))))

    # External trees with no OE recipe: fail here rather than in CMake.
    if pc & {'software-sink-encoder', 'backend-headless-egl'} and not need('V4L2WC_DIR'):
        bb.fatal("%s: software-sink-encoder / backend-headless-egl need the "
                 "v4l2-webrtc-codec sources -- set V4L2WC_DIR" % pn)
    if 'software-sink-encoder-webrtc' in pc:
        if not (pc & {'software-sink-encoder', 'backend-headless-egl'}):
            bb.fatal("%s: software-sink-encoder-webrtc needs software-sink-encoder "
                     "or backend-headless-egl" % pn)
        if not need('LIBWEBRTC_DIR') or not need('LIBWEBRTC_SO'):
            bb.fatal("%s: software-sink-encoder-webrtc needs LIBWEBRTC_DIR and "
                     "LIBWEBRTC_SO" % pn)
    if 'webview_flutter_view' in pc and not need('CEF_ROOT'):
        bb.fatal("%s: webview_flutter_view needs a CEF distribution -- set CEF_ROOT" % pn)
    if 'accesskit' in pc and not need('ACCESSKIT_DIR'):
        bb.fatal("%s: accesskit needs the accesskit-c bindings -- set ACCESSKIT_DIR" % pn)

    # Soft cases: upstream accepts these but the option does nothing, which is
    # worse than an error because the build looks like it honoured the request.
    sw_only = pc & {'software-sink-drm', 'software-sink-fbdev',
                    'software-input-libinput', 'software-sink-encoder'}
    if sw_only and 'backend-software' not in pc:
        bb.warn("%s: %s only apply to backend-software, which is not enabled"
                % (pn, ', '.join(sorted(sw_only))))
    if 'hud' in pc and 'compositor' not in pc:
        bb.warn("%s: 'hud' requires 'compositor'; CMake will force the HUD off" % pn)
    if 'compositor-dmabuf-export' in pc:
        if 'compositor' not in pc:
            bb.warn("%s: 'compositor-dmabuf-export' does nothing without 'compositor'" % pn)
        if not (pc & {'backend-wayland-vulkan', 'backend-drm-kms-vulkan'}):
            bb.warn("%s: 'compositor-dmabuf-export' exports Vulkan backing stores; "
                    "no Vulkan backend is enabled" % pn)
    egl_opts = pc & {'egl-3d', 'egl-transparency', 'egl-multisample'}
    if egl_opts and 'backend-wayland-egl' not in pc:
        bb.warn("%s: %s only apply to backend-wayland-egl, which is not enabled"
                % (pn, ', '.join(sorted(egl_opts))))
    if 'plugin-common' not in pc and 'disable-plugins' not in pc:
        others = pc & {'audioplayer_linux', 'camera', 'camera-pipewire',
                       'cloud_firestore', 'desktop_window_linux', 'file_selector',
                       'firebase_auth', 'firebase_storage', 'go_router',
                       'google_sign_in', 'integration_test', 'layer-playground-view',
                       'nav_render_view', 'pdf', 'rive-text', 'secure-storage',
                       'url_launcher', 'video-player', 'webview_flutter_view'}
        if others:
            bb.fatal("%s: the plugins below link plugin_common, so 'plugin-common' "
                     "cannot be disabled while they are enabled: %s"
                     % (pn, ', '.join(sorted(others))))
    if 'video-player-pigeon-regen' in pc and not need('VIDEO_PLAYER_LINUX_PIGEON_DIR'):
        bb.fatal("%s: video-player-pigeon-regen needs VIDEO_PLAYER_LINUX_PIGEON_DIR" % pn)
    if 'camera' in pc and 'camera-pipewire' in pc:
        bb.warn("%s: 'camera-pipewire' forces the plain camera plugin off; "
                "enabling both is redundant" % pn)
    if 'disable-plugins' in pc:
        plugins = pc & {'audioplayer_linux', 'camera', 'camera-pipewire',
                        'cloud_firestore', 'desktop_window_linux', 'file_selector',
                        'firebase_auth', 'firebase_storage', 'go_router',
                        'google_sign_in', 'integration_test', 'layer-playground-view',
                        'nav_render_view', 'pdf', 'rive-text', 'secure-storage',
                        'url_launcher', 'video-player',
                        'webview_flutter_view'}
        if plugins:
            bb.warn("%s: 'disable-plugins' overrides the enabled plugins: %s"
                    % (pn, ', '.join(sorted(plugins))))
}

EXTRA_OECMAKE += "\
    -D LLVM_CONFIG=${STAGING_BINDIR_NATIVE}/llvm-config \
    -D PLUGINS_DIR=${S}/ivi-homescreen-plugins/plugins \
    -D WAYLAND_CXX_SCANNER_EXECUTABLE=${STAGING_BINDIR_NATIVE}/wayland-cxx-scanner \
    -D WAYLAND_CXX_IME_BACKEND=${WAYLAND_CXX_IME_BACKEND} \
"

#
# ihs_shared is owned by the ivi-homescreen-shared recipe
#
# The embedder's CMake always builds shared/ (add_subdirectory(shared) is
# unconditional) and installs it, but ivi-homescreen and flutter-auto produce an
# identical libihs_shared at identical paths -- so if both shipped it, the two
# embedders could not be installed on the same machine. Drop this build's copy
# and take the library from ivi-homescreen-shared at runtime instead. The binary
# keeps its DT_NEEDED on libihs_shared.so.1, which that package provides.
#
# Both recipes default HOMESCREEN_COMMIT to the same revision, so the library
# linked here and the one installed are the same build of the same source.
#
do_install:append() {
    rm -f  ${D}${libdir}/libihs_shared.so*
    rm -f  ${D}${libdir}/pkgconfig/ivi-homescreen-shared.pc
    rm -rf ${D}${libdir}/cmake/ivi-homescreen-shared
    rm -rf ${D}${includedir}/ihs
    # Prune the directories this leaves empty, innermost first, so packaging
    # does not fail QA with "installed but not shipped" on a bare /usr/lib.
    rmdir --ignore-fail-on-non-empty \
        ${D}${libdir}/pkgconfig \
        ${D}${libdir}/cmake \
        ${D}${includedir} \
        ${D}${libdir} 2>/dev/null || true
}

RDEPENDS:${PN} += "\
    flutter-engine \
    libxkbcommon \
    ivi-homescreen-shared \
    "

INSANE_SKIP:${PN}-dbg += " buildpaths"

BBCLASSEXTEND = "verbose-logs"
