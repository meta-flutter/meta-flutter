#
# Copyright (c) 2020-2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

SUMMARY = "Toyota IVI Homescreen v3.0"

require ivi-homescreen-v3.inc

PACKAGECONFIG ??= "\
    backend-wayland-egl \
    egl-3d \
    egl-transparency \
    egl-multisample \
    \
    client-xdg \
    \
    lto \
    \
    plugin-common \
    nav_render_view \
    \
    audioplayer_linux \
    go_router \
    secure-storage \
    url_launcher \
    ${@bb.utils.contains('LICENSE_FLAGS_ACCEPTED', 'commercial', 'video-player', '', d)} \
    desktop_window_linux \
    "

EXTRA_OECMAKE += "-D EXE_OUTPUT_NAME=homescreen"

RDEPENDS_${PN} += "\
    wayland \
    "
