# Copyright (C) 2023 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

python () {
    import cipd
    bb.fetch2.methods.append(cipd.CIPD())
}

DEPENDS += " \
    ca-certificates-native \
    depot-tools-native \
    unzip-native \
"

CURL_CA_BUNDLE ??= "${STAGING_DIR_NATIVE}/etc/ssl/certs/ca-certificates.crt"
DEPOT_TOOLS ??= "${STAGING_DIR_NATIVE}/usr/share/depot_tools"
do_fetch[depends] += " \
    ca-certificates-native:do_populate_sysroot \
    depot-tools-native:do_populate_sysroot \
    unzip-native:do_populate_sysroot \
"
