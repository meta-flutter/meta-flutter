#
# Copyright (c) 2026 Joel Winarske. All rights reserved.
#
# ihs_shared: the C-ABI shared library that fronts logging, tracing,
# platform-view surface negotiation and config read-back for out-of-tree Dart
# FFI plugins and for applications built against an ivi-homescreen embedder.
#
# Built standalone from the ivi-homescreen shared/ subtree, which upstream
# supports explicitly (it establishes its own project() when it is the top-level
# CMake source). A dedicated recipe rather than a package split inside the
# embedder, for two reasons:
#
#   * ivi-homescreen and flutter-auto both build and install an identical
#     libihs_shared, so with a split each embedder would own the same paths and
#     the two could not be installed side by side. One owner makes them
#     co-installable.
#   * Consumers can DEPEND/RDEPEND on just this library without bitbake having
#     to build a whole embedder to produce it.
#
# Both embedders drop their own copy at do_install and RDEPEND on this package.
#

SUMMARY = "ivi-homescreen ihs_shared C-ABI library"
DESCRIPTION = "C-ABI shared library exposing ivi-homescreen's logging, tracing, \
platform-view negotiation and config surfaces to out-of-tree FFI plugins and \
applications. Shared by the ivi-homescreen and flutter-auto embedders."
AUTHOR = "joel.winarske@toyotaconnected.com"
HOMEPAGE = "https://github.com/toyota-connected/ivi-homescreen"
BUGTRACKER = "https://github.com/toyota-connected/ivi-homescreen/issues"
SECTION = "graphics"
CVE_PRODUCT = "homescreen"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=39ae29158ce710399736340c60147314"

# Must stay in lock-step with the embedder recipes: the .so they link at build
# time and the one this package installs have to be the same library.
HOMESCREEN_COMMIT ??= "1f5a107f20e612b14d8bdfdc3177695a30ecade0"

# gitsm: the MCP provider compiles against third_party/rapidjson, which is a
# submodule. The other submodules are unused here but the fetch is shared with
# the embedder recipes' downloads.
SRC_URI = "gitsm://github.com/toyota-connected/ivi-homescreen.git;protocol=https;branch=v3.0"
SRCREV = "${HOMESCREEN_COMMIT}"

# Only the shared/ subtree is configured; the embedder, plugins and third_party
# build graph are never entered.
OECMAKE_SOURCEPATH = "${S}/shared"

# Match the embedder's stdlib selection so the C++ runtime under the C ABI is
# the same one the shell was linked against.
TOOLCHAIN = "clang"
TOOLCHAIN_NATIVE = "clang"
TC_CXX_RUNTIME = "llvm"
PREFERRED_PROVIDER_llvm = "clang"
PREFERRED_PROVIDER_llvm-native = "clang-native"
PREFERRED_PROVIDER_libgcc = "compiler-rt"
LIBCPLUSPLUS = "-stdlib=libc++"

DEPENDS += "compiler-rt libcxx"

inherit cmake pkgconfig

#
# These three options change what is compiled into the library, so they must
# match the embedder that links it: a shell built with accessibility or MCP
# expects those symbols to be present in the .so shipped here.
#
PACKAGECONFIG ??= ""

PACKAGECONFIG[dlt] = "-DENABLE_DLT=ON,-DENABLE_DLT=OFF,,dlt-daemon"
PACKAGECONFIG[accessibility] = "-DBUILD_ACCESSIBILITY=ON,-DBUILD_ACCESSIBILITY=OFF"
PACKAGECONFIG[mcp] = "-DBUILD_MCP=ON,-DBUILD_MCP=OFF"

python () {
    pc = set((d.getVar('PACKAGECONFIG') or '').split())
    if 'mcp' in pc and 'accessibility' not in pc:
        bb.warn("%s: 'mcp' without 'accessibility' builds the MCP registry "
                "without the semantics provider" % d.getVar('PN'))
}

# The debian bbclass would republish this as libihs-shared1; consumers depend on
# it by the recipe name.
DEBIAN_NOAUTONAME_${PN} = "1"
DEBIAN_NOAUTONAME_${PN}-dev = "1"

INSANE_SKIP_${PN}-dbg += " buildpaths"
