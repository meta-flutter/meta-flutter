SUMMARY = "Wonderous Showcase"
DESCRIPTION = "A showcase app for the Flutter SDK.  Wonderous will \
               educate and entertain as you uncover information \
               about some of the most famous structures in the world."
AUTHOR = "Gskinner"
HOMEPAGE = "https://github.com/gskinnerTeam/flutter-wonderous-app"
BUGTRACKER = "https://github.com/gskinnerTeam/flutter-wonderous-app/issues"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=6570d633a333be7d4362fdecbf311bfd"

SRCREV = "6fe234f4c7fe5253f42c9168036a16fad6d28134"
SRC_URI = "git://github.com/gskinnerTeam/flutter-wonderous-app.git;lfs=0;branch=master;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "wonders"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

FLUTTER_BUILD_ARGS = "bundle"

inherit flutter-app
