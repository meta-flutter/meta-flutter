# Copyright (c) 2021-2022 Woven Alpha, Inc
# Copyright (c) 2022 Joel Winarske. All rights reserved.

python () {
    import cipd
    bb.fetch2.methods.append(cipd.CIPD())
}

DEPENDS += " \
    ca-certificates-native \
    curl-native \
    luci-go-native \
    unzip-native \
"

inherit python3native

CURL_CA_BUNDLE ??= "${STAGING_DIR_NATIVE}/etc/ssl/certs/ca-certificates.crt"
EXTRA_CIPD ??= ""

do_fetch[depends] += " \
    ca-certificates-native:do_populate_sysroot \
    curl-native:do_populate_sysroot \
    luci-go-native:do_populate_sysroot \
    unzip-native:do_populate_sysroot \
"
