#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# Where a target recipe deploys build-machine tools for a native recipe to
# stage. See meta-flutter#1009 for why they are deployed rather than packaged.
#
# Derived from DEPLOY_DIR, not DEPLOY_DIR_IMAGE. The producer is a target
# recipe and the consumer is native, and DEPLOY_DIR_IMAGE is a weak default
# that a BSP or distro may redirect per machine -- which lands on the producer
# and not on the consumer, because native.bbclass and nativesdk.bbclass both
# set MACHINEOVERRIDES = "". Two halves of one handoff must not be able to
# disagree about where it happens. MACHINE itself is untouched in those
# contexts, so these paths hold in all three variants.
#
# Still per MACHINE: the tools run on the build host but are built for a
# target, so the set deployed here is this machine's, and a shared path would
# have every machine in a multi-machine build writing the same files, which
# sstate refuses.

FLUTTER_ENGINE_HOST_TOOLS_DIR ?= "${DEPLOY_DIR}/flutter-engine-host-tools/${MACHINE}"
DART_SDK_HOST_TOOLS_DIR ?= "${DEPLOY_DIR}/dart-sdk-host-tools/${MACHINE}"

# Bump when either layout changes, and leave it in do_deploy[vardeps] on the
# producers. Moving a deploy path does not change do_deploy's signature by
# itself -- DEPLOY_DIR is in BB_HASHEXCLUDE_COMMON and sstate-outputdirs is in
# BB_SIGNATURE_EXCLUDE_FLAGS -- so without this the task does not re-run, the
# new directory stays empty, and the consumer fails on every build tree that
# already has a valid stamp. A from-scratch build would not notice.
FLUTTER_HOST_TOOLS_LAYOUT = "1"
