SUMMARY = "Flutter Video Player Example"
DESCRIPTION = "Flutter Video Player Example"
AUTHOR = "Google"
HOMEPAGE = "https://github.com/flutter/plugins/tree/main/packages/video_player"
BUGTRACKER = "https://github.com/flutter/flutter/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "\
    file://${COREBASE}/meta/files/common-licenses/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9 \
"

SRCREV = "f781b036c830952fc84a85ffa37f2f51328fa0a4"
SRC_URI = "git://github.com/flutter/plugins.git;lfs=0;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "video_player_example"
FLUTTER_APPLICATION_PATH = "packages/video_player/video_player/example"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"
FLUTTER_PREBUILD_CMD = "flutter pub get"

FLUTTER_BUILD_ARGS = "bundle"

inherit flutter-app
