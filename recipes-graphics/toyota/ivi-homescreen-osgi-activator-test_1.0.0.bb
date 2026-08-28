#
# Copyright (c) 2020-2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "osgi_activator_test"
DESCRIPTION = "Minimal OSGi bundle activator for ivi-homescreen. Registers with the shell \
over dev.osgi/bridge and reports ACTIVE, which is what releases a critical \
bundle's startup wait, so the multi-bundle harness can assert the \
critical-first ordering guarantee."

require ivi-homescreen-test.inc

# Ships a pubspec.lock, so the resolve is pinned rather than ignored.

PUBSPEC_APPNAME = "osgi_activator_test"
