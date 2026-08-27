#
# Copyright (c) 2026 Joel Winarske. All rights reserved.
#
SUMMARY = "packagekit_catalog"
DESCRIPTION = "Flutter Linux desktop app demonstrating the packagekit_dart package."
AUTHOR = "Joel Winarske"
HOMEPAGE = "https://github.com/jwinarske/packagekit_dart"
BUGTRACKER = "https://github.com/jwinarske/packagekit_dart/issues"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5416a1d50cb05af1300c65531291bd86"

# gitsm: native/third_party/sdbus-cpp is a submodule.
SRCREV = "706534b5e5b6ef36df8b0a34a2922c94c2df948f"
SRC_URI = "gitsm://github.com/jwinarske/packagekit_dart.git;branch=main;protocol=https"

SDBUS_PROVIDER = "${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'systemd', 'basu', d)}"
DEPENDS += "${SDBUS_PROVIDER}"

# hook/build.dart maps the target architecture to a cmake one and handles only
# x64 and arm64; anything else raises and takes the build down with it:
#
#   Unsupported operation: packagekit_dart does not support architecture: riscv64
#   build.dart returned with exit code: 255
#
# State that here so the recipe is skipped with a reason on an architecture the
# package does not build for, rather than failing partway through do_compile.
COMPATIBLE_HOST = "(x86_64|aarch64).*-linux"

FLUTTER_APPLICATION_PATH = "example/packagekit_catalog"
PUBSPEC_APPNAME = "packagekit_catalog"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "packagekit-dart-example-packagekit-catalog"

# The git fetcher unpacks to ${WORKDIR}/git on this release, while the default
# S is ${WORKDIR}/${BP}. Newer oe-core lines them up with
# BB_GIT_DEFAULT_DESTSUFFIX, which bitbake does not have here, so S is explicit.
S = "${WORKDIR}/git"

inherit flutter-app-native

# packagekit_nc drives the PackageKit D-Bus service. There is no packagekit
# recipe in oe-core or meta-openembedded, so the daemon must come from another
# layer; left out of RDEPENDS rather than naming an unbuildable package.
