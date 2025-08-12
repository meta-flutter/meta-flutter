#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "video_player_example"
DESCRIPTION = "Demonstrates how to use the video_player plugin."
AUTHOR = "Toyota Connected North America"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d73cf6ba84211d8b7fd0d2865b678fe8"

SRCREV = "fef58183f45e4e1802b2e2a24bd88ceb816ce957"
SRC_URI = "git://github.com/toyota-connected/tcna-packages.git;lfs=1;branch=main;protocol=https"

PUBSPEC_APPNAME = "video_player_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "toyota-connected-tcna-packages-video-player-video-player-linux-video-player-example"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "packages/video_player/video_player_linux/example"
FETCHCMD_git = "GIT_LFS_SKIP_SMUDGE=1 git "

inherit flutter-app
