#
# Copyright (c) 2024 Joel Winarske. All rights reserved.
#

SUMMARY = "Packages required to run Flutter SDK + Linux GTK embedder on target"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

RDEPENDS:${PN} += " \
    \
    flutter-engine-sdk-dev \
    \
    clang \
    cmake \
    compiler-rt-dev \
    libcxx-dev \
    ninja \
    pkgconfig \
    \
    ca-certificates \
    curl \
    git \
    \
    atk \
    cairo-dev \
    fontconfig \
    gtk+3-dev \
    libdrm-dev \
    perl \
    perl-modules \
    unzip \
    \
    gtk4-dev \
    upower-dev \
    xdg-user-dirs \
"
