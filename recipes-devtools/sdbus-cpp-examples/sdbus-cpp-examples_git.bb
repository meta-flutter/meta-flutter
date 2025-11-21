#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "sdbus-c++ examples"
DESCRIPTION = "C++ DBus examples using sdbus-cpp v2.x"
AUTHOR = "Joel Winarske <joel.winarske@gmail.com>"
HOMEPAGE = "https://github.com/jwinarske/sdbus-cpp-examples"
BUGTRACKER = "https://github.com/jwinarske/sdbus-cpp-examples/issues"
SECTION = "devtools"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=554b6aa8334cb81ca8afa6055e98a1ac"

DEPENDS += "\
    systemd \
    "

SRCREV = "7f04a089014e21dff3dfd5acd05c2599ff2b7f86"
SRC_URI = " \
    gitsm://github.com/jwinarske/sdbus-cpp-examples.git;protocol=https;branch=main \
"

inherit cmake pkgconfig 
