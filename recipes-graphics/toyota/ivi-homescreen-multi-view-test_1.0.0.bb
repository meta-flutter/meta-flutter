#
# Copyright (c) 2020-2026 Joel Winarske. All rights reserved.
#

SUMMARY = "multi_view_test"
DESCRIPTION = "Multi-monitor single-engine integration test for ivi-homescreen. One \
Flutter engine renders to N views, one per output; a shared tick counter \
proves a single engine drives every monitor."

require ivi-homescreen-test.inc

# Ships a pubspec.lock, so the resolve is pinned rather than ignored.

PUBSPEC_APPNAME = "multi_view_test"
