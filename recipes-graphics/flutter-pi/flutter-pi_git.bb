SUMMARY = "A light-weight Flutter Engine Embedder for Raspberry Pi that runs without X."
DESCRIPTION = "A light-weight Flutter Engine Embedder for Raspberry Pi. Inspired by \
               https://github.com/chinmaygarde/flutter_from_scratch. Flutter-pi also \
               runs without X11, so you don't need to boot into Raspbian Desktop & \
               have X11 and LXDE load up; just boot into the command-line."
AUTHOR = "Hannes Winkler"
HOMEPAGE = "https://github.com/ardera/flutter-pi"
BUGTRACKER = "https://github.com/ardera/flutter-pi/issues"
SECTION = "graphics"
CVE_PRODUCT = "flutter-pi"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=49fada46694956cdf2fc0292d72d888c"

DEPENDS += "\
    compiler-rt \
    libcxx \
    libdrm \
    libinput \
    libxkbcommon \
    virtual/egl \
   "

RDEPENDS:${PN} += "\
    xkeyboard-config \
    fontconfig \
    "

REQUIRED_DISTRO_FEATURES = "opengl"

SRC_URI = "git://github.com/ardera/flutter-pi.git;protocol=https;branch=master \
           file://0001-path-updates.patch"
SRCREV = "cf57b52a5848439dd4a53b657cbcb06706340ee9"

S = "${WORKDIR}/git"

inherit pkgconfig cmake features_check

require conf/include/flutter-runtime.inc

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER:libgcc = "compiler-rt"

PACKAGECONFIG ??= "text_input raw_keyboard gstreamer"

PACKAGECONFIG[text_input]   = "-DBUILD_TEXT_INPUT_PLUGIN=ON,             -DBUILD_TEXT_INPUT_PLUGIN=OFF, libinput libxkbcommon"
PACKAGECONFIG[raw_keyboard] = "-DBUILD_RAW_KEYBOARD_PLUGIN=ON,           -DBUILD_RAW_KEYBOARD_PLUGIN=OFF, libinput libxkbcommon"
PACKAGECONFIG[test]         = "-DBUILD_TEST_PLUGIN=ON,                   -DBUILD_TEST_PLUGIN=OFF"
PACKAGECONFIG[omxplayer]    = "-DBUILD_OMXPLAYER_VIDEO_PLAYER_PLUGIN=ON, -DBUILD_OMXPLAYER_VIDEO_PLAYER_PLUGIN=OFF"
PACKAGECONFIG[omx_rotation] = "-DOMXPLAYER_SUPPORTS_RUNTIME_ROTATION=ON, -DOMXPLAYER_SUPPORTS_RUNTIME_ROTATION=OFF"
PACKAGECONFIG[gstreamer]    = "-DBUILD_GSTREAMER_VIDEO_PLAYER_PLUGIN=ON, -DBUILD_GSTREAMER_VIDEO_PLAYER_PLUGIN=OFF, gstreamer1.0 gstreamer1.0-plugins-base"
PACKAGECONFIG[dump_layers]  = "-DDUMP_ENGINE_LAYERS=ON,                  -DDUMP_ENGINE_LAYERS=OFF"
PACKAGECONFIG[tsan]         = "-DENABLE_TSAN=ON,                         -DENABLE_TSAN=OFF"
PACKAGECONFIG[asan]         = "-DENABLE_ASAN=ON,                         -DENABLE_ASAN=OFF"
PACKAGECONFIG[ubsan]        = "-DENABLE_UBSAN=ON,                        -DENABLE_UBSAN=OFF"
PACKAGECONFIG[mtrace]       = "-DENABLE_MTRACE=ON,                       -DENABLE_MTRACE=OFF"

# prevent use of network to pull header
EXTRA_OECMAKE += "-D FLUTTER_EMBEDDER_HEADER=${STAGING_DIR_TARGET}/include/flutter_embedder.h"

FILES:${PN} = "\
    ${bindir} \
    "

BBCLASSEXTEND = "runtimerelease runtimeprofile runtimedebug"

DEPENDS += "flutter-engine-${@gn_get_flutter_runtime_name(d)}"
RDEPENDS:${PN} += "flutter-engine-${@gn_get_flutter_runtime_name(d)}"
