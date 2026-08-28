#
# Copyright (c) 2020-2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "mcp_drive_test"
DESCRIPTION = "Integration test app for driving ivi-homescreen over MCP."

require ivi-homescreen-test.inc

# Depends on packages/ihs_mcp_app_tools by path from the same checkout, which
# is why the whole repository is fetched rather than just the app directory.

PUBSPEC_APPNAME = "mcp_drive_test"
