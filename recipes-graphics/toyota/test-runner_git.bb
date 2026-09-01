#
# SPDX-FileCopyrightText: (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# Cap'n Proto RPC daemon that runs on a device under test beside
# ivi-homescreen, plus the client library a harness drives it with.
#
# Binds 0.0.0.0:4004 unauthenticated: test lab networks only. Needs
# CONFIG_INPUT_UINPUT and /dev/uinput to inject, /dev/input/event* to record.
#

SUMMARY = "Remote input injection and recording daemon for ivi-homescreen"
DESCRIPTION = "Cap'n Proto RPC server that injects mouse, keyboard, touchscreen \
and multi-touch events through uinput, records /dev/input events for replay, and \
captures Weston compositor screenshots. Ships the client library harnesses use."
AUTHOR = "matt.everett@toyotaconnected.com"
HOMEPAGE = "https://github.com/toyota-connected/test_runner"
BUGTRACKER = "https://github.com/toyota-connected/test_runner/issues"
SECTION = "devel"
CVE_PRODUCT = "test_runner"

# Sources say "GPLv3", which is not valid SPDX; LICENSE is the GPLv3 text.
LICENSE = "GPL-3.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1ebbd3e34237af26da5dc08a4e440464"

TEST_RUNNER_COMMIT ??= "f544fc2bd5539ec4c9d0f867a46745745314e4f4"

SRC_URI = "git://github.com/toyota-connected/test_runner.git;protocol=https;branch=main"
SRCREV = "${TEST_RUNNER_COMMIT}"
PV = "0.1+git"

inherit cmake pkgconfig capnproto features_check

# ON builds the vendored capnproto 0.9.1, which runs ./configure at configure
# time and then hands CMake a target capnp to run on the build host.
# capnproto.bbclass feeds upstream's if(NOT BUILD_CAPNP) path instead.
EXTRA_OECMAKE += "-DBUILD_CAPNP=OFF"

# Both are broken upstream (see below) and read undeclared cache variables, so
# pin them rather than trust them to stay unset.
EXTRA_OECMAKE += "-DBUILD_TESTS=OFF -DBUILD_UNIT_TESTS=OFF"

PACKAGECONFIG ??= "server examples recorder weston-screenshooter agl-health"

# Client only: the host side of a harness needs no uinput or Wayland.
PACKAGECONFIG:class-native = "weston-screenshooter agl-health"
PACKAGECONFIG:class-nativesdk = "weston-screenshooter agl-health"

# Builds libTestRunnerServer and libTestRunnerRecorder; without it the project
# produces only libTestRunnerClient.
PACKAGECONFIG[server] = "-DBUILD_SERVER=ON,-DBUILD_SERVER=OFF,libxkbcommon udev"

# Builds the executables, daemon included, so not optional in practice.
PACKAGECONFIG[examples] = "-DBUILD_EXAMPLES=ON,-DBUILD_EXAMPLES=OFF"

# libTestRunnerRecorder, the TestRunner-Recorder tool and the server's Recorder
# RPC methods. OFF leaves those methods answering "unimplemented", so clients
# need no rebuild.
PACKAGECONFIG[recorder] = "-DBUILD_RECORDER=ON,-DBUILD_RECORDER=OFF"

# Recorder input backend: libinput instead of raw evdev. Off matches upstream CI.
PACKAGECONFIG[libinput] = "-DENABLE_LIBINPUT=ON,-DENABLE_LIBINPUT=OFF,libinput"

# Only the plugin's server half links wayland-client, so that dependency is
# added by server + this option together, below.
PACKAGECONFIG[weston-screenshooter] = "-DENABLE_PLUGIN_WESTON_SCREENSHOOTER=ON,\
    -DENABLE_PLUGIN_WESTON_SCREENSHOOTER=OFF"

PACKAGECONFIG[agl-health] = "-DENABLE_PLUGIN_AGL_HEALTH=ON,-DENABLE_PLUGIN_AGL_HEALTH=OFF,curl"

# Instrumented binaries only; the coverage targets run the tests and lcov on the
# build host. GCOV_EXECUTABLE is seeded because coverage.cmake probes it
# REQUIRED and no native gcov answers to that name.
PACKAGECONFIG[coverage] = "-DENABLE_COVERAGE=ON \
    -DGCOV_EXECUTABLE=${STAGING_BINDIR_TOOLCHAIN}/${TARGET_PREFIX}gcov,\
    -DENABLE_COVERAGE=OFF,lcov-native"

#
# Not exposed:
#
#   BUILD_TESTS       compiles kj_client.cpp and kj_server.cpp, absent from the
#                     tree, and depends on a renamed target
#   BUILD_UNIT_TESTS  hard-codes the vendored capnproto 0.9.1 include path,
#                     searched ahead of the sysroot while linking target libcapnp
#

DEPENDS += "${@bb.utils.contains('PACKAGECONFIG', 'server', \
    bb.utils.contains('PACKAGECONFIG', 'weston-screenshooter', 'wayland', '', d), '', d)}"

REQUIRED_DISTRO_FEATURES = "${@bb.utils.contains('PACKAGECONFIG', 'server', \
    bb.utils.contains('PACKAGECONFIG', 'weston-screenshooter', 'wayland', '', d), '', d)}"

python () {
    pc = set((d.getVar('PACKAGECONFIG') or '').split())
    pn = d.getVar('PN')

    if 'examples' in pc and 'server' not in pc:
        bb.fatal("%s: 'examples' links the libraries 'server' builds -- enable both" % pn)

    if 'server' in pc and 'examples' not in pc:
        bb.warn("%s: 'server' without 'examples' ships libraries but no daemon" % pn)

    if 'recorder' in pc and 'server' not in pc:
        bb.warn("%s: 'recorder' needs 'server'; the client builds no recorder" % pn)

    if 'libinput' in pc and 'recorder' not in pc:
        bb.warn("%s: 'libinput' is the recorder's input backend, and 'recorder' is off" % pn)
}

# No SOVERSION on any of the libraries, so keep the .so files out of -dev.
SOLIBS = ".so"
FILES_SOLIBSDEV = ""

BBCLASSEXTEND = "native nativesdk"
