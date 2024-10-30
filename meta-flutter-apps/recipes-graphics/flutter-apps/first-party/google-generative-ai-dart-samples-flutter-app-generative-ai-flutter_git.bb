#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "generative_ai_flutter"
DESCRIPTION = "Sample app for the google_generative_ai package"
AUTHOR = "Google"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=175792518e4ac015ab6696d16c4f607e"

SRCREV = "76ae8f89eed4789af0be9e7876d0089909abc419"
SRC_URI = "git://github.com/google/generative-ai-dart.git;lfs=1;branch=main;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "generative_ai_flutter"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "google-generative-ai-dart-samples-flutter-app-generative-ai-flutter"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "samples/flutter_app"
GOOGLE_API_KEY ??= ""
FLUTTER_BUILD_ARGS = "bundle --dart-define API_KEY=${GOOGLE_API_KEY}"
APP_AOT_EXTRA = "-DAPI_KEY=${GOOGLE_API_KEY}"

inherit flutter-app
