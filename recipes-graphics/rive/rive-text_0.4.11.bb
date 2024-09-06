#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "rive_common"
DESCRIPTION = "Rive Text Shared Module"
AUTHOR = "Rive"
HOMEPAGE = "https://github.com/rive-app/rive-flutter"
BUGTRACKER = "https://github.com/rive-app/rive-flutter"
SECTION = "devtools"

LICENSE = "MIT & Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=c52243a14a066c83e50525d9ad046678"

SRCREV = "65a29b28f10f9347626bbfd9a13928f90bd3d6c6"
SRC_URI = "git://github.com/meta-flutter/rive-common.git;protocol=https;lfs=0;nobranch=1"

S = "${WORKDIR}/git"

inherit cmake

FILES:${PN} = "${libdir}"
