# Copyright (c) 2021-2022 Woven Alpha, Inc

python () {
    import cipd
    bb.fetch2.methods.append(cipd.CIPD())
}

DEPENDS += " \
    curl-native \
    ca-certificates-native \
    ninja-native \
    depot-tools-native \
    tar-native \
    xz-native \
    zip-native \
    unzip-native \
"

inherit python3native

CURL_CA_BUNDLE ??= "${STAGING_DIR_NATIVE}/etc/ssl/certs/ca-certificates.crt"
DEPOT_TOOLS ??= "${STAGING_DIR_NATIVE}/usr/share/depot_tools"
PYTHON2_PATH ??= "bootstrap-2@3.8.9.chromium.14_bin/python/bin"
EXTRA_CIPD ??= ""
do_fetch[depends] += " \
    curl-native:do_populate_sysroot \
    ca-certificates-native:do_populate_sysroot \
    depot-tools-native:do_populate_sysroot \
    zip-native:do_populate_sysroot \
    unzip-native:do_populate_sysroot \
    fontconfig:do_populate_sysroot \
"

