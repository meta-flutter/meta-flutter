#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "rive_example"
DESCRIPTION = "A collection of Rive Flutter examples"
AUTHOR = "Rive"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c52243a14a066c83e50525d9ad046678"

SRCREV = "870234fddc8e6aae3d29ef1dd63fb8e55aba5802"
SRC_URI = " \
    git://github.com/rive-app/rive-flutter.git;lfs=1;nobranch=1;protocol=https;destsuffix=git \
    git://github.com/meta-flutter/rive-common.git;protocol=https;lfs=0;nobranch=1;destsuffix=rive_common \
    file://rive-flutter/0001-Changes-for-generic-Linux.patch \ 
"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "rive_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "rive-app-rive-flutter-example-rive-example"
FLUTTER_APPLICATION_PATH = "example"

do_patch:append() {
    RIVE_COMMON_PATH=${S}/rive_common
    sed -i "s|../rive-common|${RIVE_COMMON_PATH}|g" pubspec.yaml
}

inherit flutter-app

RDEPENDS:${PN} += " \
    rive-taffy-ffi \
    rive-text \
"
