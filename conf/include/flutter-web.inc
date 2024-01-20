#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#
# Helper class for building Flutter Web Applications.
# Assumes that:
# - S is defined and points to source directory.
# - PUBSPEC_APPNAME is defined correctly.  This is the name value from pubspec.yml.
#

require conf/include/flutter-version.inc
require conf/include/app-utils.inc
require conf/include/common.inc

DEPENDS += " \
    cmake-native \
    ninja-native \
    "

BUILD_TYPE = "web"

FLUTTER_BUILD_ARGS ??= "--no-tree-shake-icons"

do_install() {

    bbnote "Flutter web app: Installing"
    
    # App artifacts
    install -d ${D}${FLUTTER_INSTALL_DIR}/
    cp -r ${S}/${FLUTTER_APPLICATION_PATH}build/web/* ${D}${FLUTTER_INSTALL_DIR}/
}

FILES:${PN} = "\
    ${FLUTTER_INSTALL_DIR} \
    "
