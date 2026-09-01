#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# Shared bits for an app that ships inside the Flutter SDK.
#
# These are not third-party repositories and they need no source of their own.
# flutter-sdk-native is already a dependency of every flutter-app recipe
# (conf/include/common.inc), so the SDK -- and with it every app it ships -- is
# staged in recipe-sysroot-native before this recipe configures. Fetching the
# release archive again to get at source already on disk would cost a 1.5 GB
# download and a 2.5 GB unpack per app.
#
# Copy the workspace out of the sysroot instead, at about 127 MB:
#
#   pubspec.yaml           the workspace root
#   packages dev examples  every member it lists, which all must exist
#   LICENSE                for LIC_FILES_CHKSUM
#
# Everything left behind is what made the archive large and none of it is read
# from ${S}: bin (1.8 GB) is the toolchain, which the build takes from
# ${FLUTTER_SDK}; .git and .pub-preload-cache are not source.
#
# The whole workspace comes across rather than the one app because SDK apps
# declare "resolution: workspace" and pub requires every member the root
# pubspec lists to exist. conf/include/common.inc already follows
# .dart_tool/pub/workspace_ref.json to that root when building the snapshot.
#
# A recipe using this sets PUBSPEC_APPNAME and FLUTTER_APPLICATION_PATH.

# flutter-version.inc is not required here: flutter-app.inc pulls it in, and
# requiring it twice warns. FLUTTER_SDK_VERSION comes from common.inc.

DESCRIPTION ?= "Flutter SDK example application"
AUTHOR = "Google"
HOMEPAGE = "https://github.com/flutter/flutter"
BUGTRACKER = "https://github.com/flutter/flutter/issues"
SECTION = "graphics"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1d84cf16c48e571923f837136633a265"

# No source of its own; see above.
SRC_URI = ""

S = "${UNPACKDIR}/flutter-sdk-workspace"

# The app is whatever the pinned SDK ships, so that is its version.
PV = "${FLUTTER_SDK_VERSION}"

# Keep the lockfile. Unlike the third-party apps, the SDK ships a real one --
# at the workspace root, covering all 78 members, and `flutter update-packages`
# during the SDK build leaves it byte-identical. It is an authored resolution,
# so there is nothing to gain by discarding it and re-resolving per build.
PUBSPEC_IGNORE_LOCKFILE = "0"

FLUTTER_APPLICATION_INSTALL_SUFFIX ?= "flutter-sdk-${@(d.getVar('FLUTTER_APPLICATION_PATH') or '').replace('/', '-').replace('_', '-')}"

# pubspec.lock is not optional here. A workspace member resolves against the
# root lockfile -- `pub get --enforce-lockfile` from the member reports
# "Cannot do --enforce-lockfile without an existing pubspec.lock ... to create
# ../../pubspec.lock" -- and without it the resolve fails, the root .dart_tool
# is never written, and common.inc falls back to a member package_config.json
# that a workspace member does not have.
SDK_WORKSPACE_PATHS ?= "pubspec.yaml pubspec.lock LICENSE analysis_options.yaml packages dev examples"

# Ordered against three things, all of which matter:
#
#   after do_unpack  -- do_unpack[cleandirs] is UNPACKDIR, and S lives under
#                       it, so staging first would be undone
#   after do_prepare_recipe_sysroot -- that is what puts the SDK in place
#   before do_patch  -- so ${S} exists by the time do_populate_lic reads
#                       LICENSE
#
# S has to be under UNPACKDIR rather than WORKDIR: insane.bbclass:1450 makes
# anything else fatal.
do_stage_sdk_workspace() {
    rm -rf ${S}
    install -d ${S}
    for p in ${SDK_WORKSPACE_PATHS}; do
        if [ ! -e "${FLUTTER_SDK}/$p" ]; then
            bbfatal "the staged Flutter SDK has no $p; ${FLUTTER_SDK} is not a full SDK"
        fi
        cp -a "${FLUTTER_SDK}/$p" ${S}/
    done
    if [ ! -f "${S}/${FLUTTER_APPLICATION_PATH}/pubspec.yaml" ]; then
        bbfatal "no app at ${FLUTTER_APPLICATION_PATH} in the SDK this layer pins (${FLUTTER_SDK_VERSION})"
    fi
}
addtask stage_sdk_workspace after do_unpack do_prepare_recipe_sysroot before do_patch

inherit flutter-app

# Vendor the workspace's pub cache, so the build resolves offline.
#
# SDK apps are pub workspace members: there is no per-app lockfile, only one at
# the workspace root covering all 78 members. So one fragment serves every SDK
# app, and its cost does not grow as recipes are added -- the opposite of the
# third-party apps, where each needs its own.
#
# After `inherit flutter-app`, not before: conf/include/common.inc assigns
# PUB_CACHE = "${WORKDIR}/pub_cache" with a hard =, and flutter-app pulls it
# in. Inheriting pub-cache first lets that assignment win, which puts the
# cache back in the directory the packages are not unpacked into -- the
# defect #862 fixed, reintroduced by ordering alone.
#
# PUBSPEC_APP_DIR is left at its default of ${S}, which for these recipes is
# the workspace root. That is where pub has to resolve a workspace, and where
# the lockfile the fragment was generated from already sits: it arrives with
# SDK_WORKSPACE_PATHS rather than through SRC_URI, so PUBSPEC_LOCK_FILE stays
# unset and pub-cache.bbclass skips installing one.
SDK_WORKSPACE_VENDOR ?= "1"
SDK_WORKSPACE_FRAGMENT ?= "${LAYERDIR_meta-flutter}/recipes-graphics/flutter-sdk/apps/flutter-sdk-workspace-pubcache.inc"

require ${@'recipes-graphics/flutter-sdk/apps/flutter-sdk-workspace-pubcache.inc' if d.getVar('SDK_WORKSPACE_VENDOR') == '1' else ''}

inherit ${@'pub-cache' if d.getVar('SDK_WORKSPACE_VENDOR') == '1' else ''}
