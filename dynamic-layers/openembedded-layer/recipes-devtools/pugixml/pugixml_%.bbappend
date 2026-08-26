#
# Copyright (c) 2026 Joel Winarske. All rights reserved.
#
# Build pugixml for the host as well as the target.
#
# wayland-cxx-scanner is a build-host code generator, so ivi-homescreen v3 needs
# wayland-cxx-scanner-native, which needs pugixml-native. meta-oe carries
# BBCLASSEXTEND = "native nativesdk" on pugixml from kirkstone onward, but this
# release's pugixml_1.10.bb has no BBCLASSEXTEND at all, so there is no native
# variant to depend on:
#
#   ERROR: Nothing PROVIDES 'pugixml-native' (but ...wayland-cxx-scanner_1.0.0.bb
#     DEPENDS on or otherwise requires it)
#   Missing or unbuildable dependency chain was:
#     ['ivi-homescreen', 'wayland-cxx-scanner-native', 'pugixml-native']
#
# Nothing else differs: it is a plain cmake library and the later meta-oe
# versions add the same line and nothing more.
#
BBCLASSEXTEND = "native"

# The scanner prefers find_package(pugixml CONFIG) and falls back to
# pkg-config. 1.10 predates meta-oe passing BUILD_PKGCONFIG, so enable it here
# to keep that fallback available. Harmless where the CMake config package is
# found first.
EXTRA_OECMAKE += "-DBUILD_PKGCONFIG=ON"
