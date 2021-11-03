SUMMARY = "A light-weight Flutter Engine Embedder for Raspberry Pi that runs without X."
DESCRIPTION = "A light-weight Flutter Engine Embedder for Raspberry Pi. Inspired by \
               https://github.com/chinmaygarde/flutter_from_scratch. Flutter-pi also \
               runs without X11, so you don't need to boot into Raspbian Desktop & \
               have X11 and LXDE load up; just boot into the command-line."
AUTHOR = "Hannes Winkler"
HOMEPAGE = "https://github.com/ardera/flutter-pi"
BUGTRACKER = "https://github.com/ardera/flutter-pi/issues"
SECTION = "graphics"
CVE_PRODUCT = ""
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=49fada46694956cdf2fc0292d72d888c"

DEPENDS += "\
    flutter-engine \
    libdrm \
    libinput \
    libxkbcommon \
    virtual/egl \
   "

RDEPENDS:${PN} += "\
    xkeyboard-config \
    fontconfig \
    "

SRC_URI = "git://github.com/ardera/flutter-pi.git;protocol=https;branch=master \
           file://0001-icudtl.dat-path.patch"
SRCREV = "e000e5777a86944a9e627364c8ab654408022351"

S = "${WORKDIR}/git"

inherit pkgconfig cmake

EXTRA_OECMAKE += "-D BUILD_TEXT_INPUT_PLUGIN=ON \
                  -D BUILD_RAW_KEYBOARD_PLUGIN=ON \
                  -D BUILD_TEST_PLUGIN=OFF \
                  -D BUILD_OMXPLAYER_VIDEO_PLAYER_PLUGIN=ON \
                  "

FILES:${PN} = "${bindir}"

BBCLASSEXTEND = ""
