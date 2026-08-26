#
# Copyright (c) 2026 Joel Winarske. All rights reserved.
#

SUMMARY = "Wayland protocol scanner generating C++17 CRTP client/server headers"
DESCRIPTION = "\
wayland-cxx-scanner parses Wayland XML protocol files and generates \
WTL-patterned C++17 client and server headers using CRTP, along with a \
header-only framework (wl::CProxy, wl::CRegistry, etc.). \
ivi-homescreen v3 consumes the framework as a submodule and invokes the \
scanner as a build-host code generator, so a -native build of this recipe \
is a build dependency of every Wayland-backend ivi-homescreen build."

HOMEPAGE = "https://github.com/jwinarske/wayland-cxx-scanner"
BUGTRACKER = "https://github.com/jwinarske/wayland-cxx-scanner/issues"
SECTION = "devel"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8992d37861cd7e48b171be43673f1d7f"

# Normally pinned to the commit ivi-homescreen v3.0 carries as
# third_party/wayland-cxx-scanner, so the host code generator and the vendored
# wl/ framework stay in lock-step.
#
# Two commits ahead of that pin for now. jwinarske/wayland-cxx-scanner#130 is
# needed here: pugixml exports its CMake config without a NAMESPACE before 1.12,
# so on this release find_package(pugixml CONFIG) succeeds while defining only
# the unnamespaced target, and the scanner failed configure asking for
# pugixml::pugixml. The range is CMakeLists.txt only -- no wl/ header moved --
# so the generator and the framework are still the same code, and the lock-step
# this comment describes holds in substance. Drop back to a single pin when
# ivi-homescreen next rolls its submodule.
SRCREV ??= "75575fe3e78e95796f2b9abb51f8314cbf9c31b4"
SRC_URI = "git://github.com/jwinarske/wayland-cxx-scanner.git;protocol=https;branch=main"

DEPENDS = "pugixml"

# The git fetcher unpacks to ${WORKDIR}/git on this release, while the default
# S is ${WORKDIR}/${BP}. Newer oe-core lines them up with
# BB_GIT_DEFAULT_DESTSUFFIX, which bitbake does not have here, so S is explicit.
S = "${WORKDIR}/git"

inherit cmake pkgconfig

# The IME backend is a compositor capability, not a project default: GNOME/Mutter
# speak text-input-v3, Weston/AGL stacks speak text-input-v1. Set it from the
# compositor/BSP layer (?= beats this ??=) or from local.conf. Valid values:
#   none text-input-v1 text-input-v3 input-method-v1 input-method-v2
#   virtual-keyboard-v1
# Only affects the framework headers, not the scanner executable.
WAYLAND_CXX_IME_BACKEND ??= "text-input-v3"
WAYLAND_CXX_WERROR ??= "OFF"

EXTRA_OECMAKE = "\
    -D WAYLAND_CXX_WERROR=${WAYLAND_CXX_WERROR} \
    -D WAYLAND_CXX_IME_BACKEND=${WAYLAND_CXX_IME_BACKEND} \
"

PACKAGECONFIG ??= ""

# Ship a target-arch scanner on the device. Under cross the CMake build only
# compiles the executable when this is on; the host code generator comes from
# the -native build, which always builds and installs it.
PACKAGECONFIG[tool] = "-DWAYLAND_CXX_SCANNER_BUILD_TOOL=ON,-DWAYLAND_CXX_SCANNER_BUILD_TOOL=OFF,"
PACKAGECONFIG[tests] = "-DWAYLAND_CXX_SCANNER_BUILD_TESTS=ON,-DWAYLAND_CXX_SCANNER_BUILD_TESTS=OFF,googletest"

# Header-only framework + its CMake package and pkg-config file.
FILES_${PN}-dev += "\
    ${includedir}/wl \
    ${libdir}/cmake/wayland-cxx-scanner \
    ${libdir}/pkgconfig/wayland-cxx.pc \
"

ALLOW_EMPTY_${PN} = "1"

BBCLASSEXTEND = "native nativesdk"

DEPENDS_class-native = "pugixml-native"
DEPENDS_class-nativesdk = "pugixml-nativesdk"

# The host code generator is the whole point of these variants, so they must
# build and install the executable. (A native build forces BUILD_TOOL on
# regardless; nativesdk cross-compiles and would not without this.)
PACKAGECONFIG_class-native = "tool"
PACKAGECONFIG_class-nativesdk = "tool"
