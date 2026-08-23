#
# Copyright (c) 2026 Joel Winarske. All rights reserved.
#
SUMMARY = "smoke_render"
DESCRIPTION = "Deterministic render smoke tests for flutter_scene. Draws a set \
of fixed scenes through Flutter GPU and Impeller, each asserting a sane frame \
(centre coverage, clear corners), which is the cheapest way to catch a broken \
backend or a scene that drew nothing."
AUTHOR = "Brandon DeRosier"
HOMEPAGE = "https://github.com/bdero/flutter_scene"
BUGTRACKER = "https://github.com/bdero/flutter_scene/issues"
SECTION = "graphics"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=bd3b5df74c82765dfbbb94cbc5e3b7ff"

SRCREV = "93d420be938213d19744723f8f6bb56be301187d"
SRC_URI = "git://github.com/bdero/flutter_scene.git;branch=master;protocol=https"

# smoke_render is a pub workspace member: it declares `resolution: workspace`
# and the repository root owns the lockfile. Resolve from the root and build
# the member. Without this pub refuses to operate in the member directory.
#
# The distinction matters beyond mechanics: the app depends on
# flutter_scene ^0.23.0 by version, and the repository carries its own
# packages/flutter_scene at 0.23.0. Workspace resolution binds to that local
# copy, which is what the example is developed against; resolving the member
# standalone would silently take the published 0.23.0 instead.
FLUTTER_PUB_ROOT = ""
FLUTTER_APPLICATION_PATH = "examples/smoke_render"
PUBSPEC_APPNAME = "smoke_render"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "flutter-scene-example-smoke-render"

# The build hook compiles the scene materials and shader bundles through
# impellerc, which flutter-engine ships in engine_sdk.zip. flutter_scene depends
# on code_assets as well as data_assets, so a hook may emit native code; the
# native class covers that and is otherwise equivalent.
inherit flutter-app-native

# Flutter GPU content needs Impeller on Vulkan at run time. ivi-homescreen
# reaches the engine switch with:
#
#   homescreen --backend wayland-vulkan --enable-impeller \
#              --engine-arg=--enable-flutter-gpu -b <bundle>
#
# so the image needs a Vulkan driver for its GPU. That is a machine choice
# rather than something this recipe can express, which is why it is a comment
# and not an RDEPENDS.
