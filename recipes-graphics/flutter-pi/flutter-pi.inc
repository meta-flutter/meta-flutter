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

RDEPENDS_${PN} += "\
    xkeyboard-config \
    fontconfig \
    "

REQUIRED_DISTRO_FEATURES = "opengl"

SRC_URI = "git://github.com/ardera/flutter-pi.git;protocol=https;branch=master \
           file://0001-path-updates.patch"
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

inherit pkgconfig cmake features_check

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER_libgcc = "compiler-rt"

PACKAGECONFIG ??= "text_input raw_keyboard omxplayer"

PACKAGECONFIG[text_input]   = "-DBUILD_TEXT_INPUT_PLUGIN=ON,             -DBUILD_TEXT_INPUT_PLUGIN=OFF"
PACKAGECONFIG[raw_keyboard] = "-DBUILD_RAW_KEYBOARD_PLUGIN=ON,           -DBUILD_RAW_KEYBOARD_PLUGIN=OFF"
PACKAGECONFIG[test]         = "-DBUILD_TEST_PLUGIN=ON,                   -DBUILD_TEST_PLUGIN=OFF"
PACKAGECONFIG[omxplayer]    = "-DBUILD_OMXPLAYER_VIDEO_PLAYER_PLUGIN=ON, -DBUILD_OMXPLAYER_VIDEO_PLAYER_PLUGIN=OFF"

FILES_${PN} = "\
    ${bindir} \
    "

BBCLASSEXTEND = ""
