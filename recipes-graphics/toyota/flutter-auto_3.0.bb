#
# Copyright (c) 2020-2026 Joel Winarske. All rights reserved.
#

SUMMARY = "Toyota IVI Homescreen v3.0 - AGL variant"

require ivi-homescreen-v3.inc

PACKAGECONFIG ??= "\
    backend-wayland-egl \
    egl-3d \
    egl-transparency \
    egl-multisample \
    \
    client-xdg \
    client-agl-shell \
    \
    ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'wdt_systemd', '', d)} \
    \
    lto \
    \
    plugin-common \
    nav_render_view \
    \
    audioplayer_linux \
    go_router \
    url_launcher \
    "

EXTRA_OECMAKE += "-D EXE_OUTPUT_NAME=${PN}"
