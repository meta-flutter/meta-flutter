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

SRC_URI = " \
    git://github.com/rive-app/rive-flutter.git;lfs=1;branch=master;protocol=https;destsuffix=git;name=rive_flutter \
    git://github.com/meta-flutter/rive-common.git;protocol=https;lfs=0;nobranch=1;destsuffix=rive_common;name=rive_common \
    file://rive-flutter/0001-Changes-for-generic-Linux.patch \ 
"
SRCREV_FORMAT .= "_rive_flutter"
SRCREV_rive_flutter = "870234fddc8e6aae3d29ef1dd63fb8e55aba5802"
SRCREV_FORMAT .= "_rive_common"
SRCREV_rive_common = "9de5393c689e9e95e410d88a780772e42eb1e760"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "rive_example"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "rive-app-rive-flutter-example-rive-example"
FLUTTER_APPLICATION_PATH = "example"

# patch pubspec to use local rive_common
python do_compile:prepend() {
    rive_common_path = os.path.join(d.getVar("WORKDIR"),'rive_common')
    pubspec_yaml_path = os.path.join(d.getVar("WORKDIR"),'git','pubspec.yaml')
    with open(pubspec_yaml_path, 'r') as file:
        data = file.read()
        data = data.replace('@RIVE_COMMON_PATH@', rive_common_path)
    with open(pubspec_yaml_path, 'w') as file:
        file.write(data)
}


inherit flutter-app

RDEPENDS:${PN} += " \
    rive-text \
    rive-taffy-ffi \
"
