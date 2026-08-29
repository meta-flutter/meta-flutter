#
# Copyright (c) 2026 Ahmed Wafdy. All rights reserved.
#
SUMMARY = "XDG Desktop Portal backend for AGL (Automotive Grade Linux)"
DESCRIPTION = "A xdg-desktop-portal backend for AGL that provides portal APIs \
               for sandboxed applications to access desktop features."
AUTHOR = "Ahmed Wafdy <ahmedadelwafdy782@gmail.com>"
HOMEPAGE = "https://github.com/flatpak-minimal/xdg-desktop-portal-agl"
BUGTRACKER = "https://github.com/flatpak-minimal/xdg-desktop-portal-agl/issues"
SECTION = "support"

LICENSE = "GPL-2.0-or-later"
LIC_FILES_CHKSUM = "file://LICENSE;md5=570a9b3749dd0463a1778803b12a6dce"

SRC_URI = "git://github.com/flatpak-minimal/xdg-desktop-portal-agl.git;protocol=https;branch=main"
SRCREV = "${AUTOREV}"
PV = "0.1.0+git"

DEPENDS = "wayland"
RDEPENDS:${PN} = "xdg-desktop-portal wayland"

inherit cargo cargo-update-recipe-crates pkgconfig

require ${BPN}-crates.inc

# Upstream Cargo.toml sets [profile.release] strip = true, which trips the already-stripped QA check.
export CARGO_PROFILE_RELEASE_STRIP = "false"

do_install() {
    # binary (portal backends live under libexec)
    install -d ${D}${libexecdir}
    install -m 0755 ${B}/target/${CARGO_TARGET_SUBDIR}/xdg-desktop-portal-agl \
        ${D}${libexecdir}/

    # systemd user services
    install -d ${D}${systemd_user_unitdir}
    sed -e 's,/usr/libexec/,${libexecdir}/,g' \
        -e 's,/usr/bin/,${bindir}/,g' \
        ${S}/data/xdg-desktop-portal-agl.service.in \
        > ${D}${systemd_user_unitdir}/xdg-desktop-portal-agl.service
    sed -e 's,/usr/libexec/,${libexecdir}/,g' \
        -e 's,/usr/bin/,${bindir}/,g' \
        ${S}/data/weston-environment.service \
        > ${D}${systemd_user_unitdir}/xdg-desktop-portal-agl-environment.service
    chmod 0644 ${D}${systemd_user_unitdir}/xdg-desktop-portal-agl.service \
        ${D}${systemd_user_unitdir}/xdg-desktop-portal-agl-environment.service

    install -d ${D}${systemd_user_unitdir}/default.target.wants
    ln -sf ../xdg-desktop-portal-agl-environment.service \
        ${D}${systemd_user_unitdir}/default.target.wants/xdg-desktop-portal-agl-environment.service

    # enable portal under xdg-desktop-portal.service
    install -d ${D}${systemd_user_unitdir}/xdg-desktop-portal.service.wants
    ln -sf ../xdg-desktop-portal-agl.service \
        ${D}${systemd_user_unitdir}/xdg-desktop-portal.service.wants/xdg-desktop-portal-agl.service

    # portal descriptor
    install -d ${D}${datadir}/xdg-desktop-portal/portals
    install -m 0644 ${S}/data/agl.portal \
        ${D}${datadir}/xdg-desktop-portal/portals/

    # portal config
    install -d ${D}${datadir}/xdg-desktop-portal
    install -m 0644 ${S}/data/agl-portals.conf \
        ${D}${datadir}/xdg-desktop-portal/

    # D-Bus activation
    install -d ${D}${datadir}/dbus-1/services
    sed -e 's,/usr/libexec/,${libexecdir}/,g' \
        -e 's,/usr/bin/,${bindir}/,g' \
        ${S}/data/org.freedesktop.impl.portal.desktop.agl.service \
        > ${D}${datadir}/dbus-1/services/org.freedesktop.impl.portal.desktop.agl.service
    chmod 0644 ${D}${datadir}/dbus-1/services/org.freedesktop.impl.portal.desktop.agl.service

    # profile.d
    install -d ${D}${sysconfdir}/profile.d
    install -m 0644 ${S}/data/agl-portal.sh \
        ${D}${sysconfdir}/profile.d/
}

FILES:${PN} += " \
    ${libexecdir}/xdg-desktop-portal-agl \
    ${systemd_user_unitdir}/xdg-desktop-portal-agl.service \
    ${systemd_user_unitdir}/xdg-desktop-portal-agl-environment.service \
    ${systemd_user_unitdir}/default.target.wants/xdg-desktop-portal-agl-environment.service \
    ${systemd_user_unitdir}/xdg-desktop-portal.service.wants/xdg-desktop-portal-agl.service \
    ${datadir}/xdg-desktop-portal/portals/agl.portal \
    ${datadir}/xdg-desktop-portal/agl-portals.conf \
    ${datadir}/dbus-1/services/org.freedesktop.impl.portal.desktop.agl.service \
    ${sysconfdir}/profile.d/agl-portal.sh \
"
