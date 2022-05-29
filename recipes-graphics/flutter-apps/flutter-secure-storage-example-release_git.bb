SUMMARY = "Flutter Secure Storage Example"
DESCRIPTION = "Flutter Secure Storage Example"
AUTHOR = "mogol"
HOMEPAGE = "https://github.com/mogol/flutter_secure_storage"
BUGTRACKER = "https://github.com/mogol/flutter_secure_storage/issues"
SECTION = "graphics"

LICENSE = "CLOSED"

FLUTTER_RUNTIME = "release"

# DEPENDS += "\
#    jsoncpp \
#    libsecret \
#    util-linux \
#    xz \
#    "

SRCREV = "c371ea0c2d127b62316bbac52666254ad8cac844"
SRC_URI = "git://github.com/meta-flutter/flutter_secure_storage.git;lfs=0;branch=jw/yocto;protocol=https;destsuffix=git"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "flutter_secure_storage_example"
FLUTTER_APPLICATION_PATH = "flutter_secure_storage/example"
FLUTTER_APPLICATION_INSTALL_PREFIX = "/flutter"

FLUTTER_BUILD_ARGS = "bundle --no-pub -v"
#FLUTTER_APP_DISABLE_NATIVE_PLUGINS = "1"
#FLUTTER_REMOVE_LINUX_BUILD_ARTIFACTS = "1"

inherit flutter-app
