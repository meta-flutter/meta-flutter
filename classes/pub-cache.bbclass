# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# pub-cache.bbclass - consume a pubvendor.py-generated .inc
#
# Pairs with the SRC_URI fragment produced by tools/pubvendor/pubvendor.py:
#   * points PUB_CACHE at the staged tree
#   * asserts the in-tree pubspec.lock matches the lockfile the .inc was
#     generated from (PUBSPEC_LOCK_SHA256)
#   * synthesizes hosted-hashes/<host>/<pkg>-<ver>.sha256 files from the
#     SRC_URI checksum flags, so newer Dart SDKs' content verification
#     passes offline
#   * runs `dart pub get --offline --enforce-lockfile`
#
# The recipe must set PUBSPEC_APP_DIR to the app source dir containing
# pubspec.yaml/pubspec.lock (default ${S}).

PUB_CACHE_LOCAL ?= "pub_cache"
PUB_CACHE = "${WORKDIR}/${PUB_CACHE_LOCAL}"
PUBSPEC_APP_DIR ?= "${S}"

export PUB_CACHE

python do_check_pubspec_lock() {
    import hashlib, os

    expected = d.getVar('PUBSPEC_LOCK_SHA256')
    if not expected:
        bb.warn("PUBSPEC_LOCK_SHA256 unset; lockfile integrity not enforced")
        return
    lock = os.path.join(d.getVar('PUBSPEC_APP_DIR'), 'pubspec.lock')
    if not os.path.isfile(lock):
        bb.fatal("pubspec.lock not found at %s" % lock)
    with open(lock, 'rb') as f:
        actual = hashlib.sha256(f.read()).hexdigest()
    if actual != expected:
        bb.fatal(
            "pubspec.lock sha256 mismatch: recipe .inc was generated from a "
            "different lockfile (expected %s, got %s). Re-run pubvendor.py."
            % (expected, actual))
}
addtask check_pubspec_lock after do_unpack before do_configure

python do_write_hosted_hashes() {
    # Newer Dart SDKs verify hosted package content hashes against
    # $PUB_CACHE/hosted-hashes/<host>/<pkg>-<ver>.sha256. Derive them
    # from the SRC_URI entries the .inc already carries.
    import os
    from bb.fetch2 import decodeurl

    pub_cache = d.getVar('PUB_CACHE')
    marker = '/hosted/'
    for uri in (d.getVar('SRC_URI') or '').split():
        _, _, _, _, _, parm = decodeurl(uri)
        subdir = parm.get('subdir', '')
        name = parm.get('name')
        idx = subdir.find(marker)
        if idx < 0 or not name:
            continue
        sha = d.getVarFlag('SRC_URI', name + '.sha256sum')
        if not sha:
            bb.warn("no sha256sum flag for SRC_URI name=%s" % name)
            continue
        # subdir tail: hosted/<encoded-host>/<pkg>-<ver>
        host, pkgver = subdir[idx + len(marker):].split('/', 1)
        dst = os.path.join(pub_cache, 'hosted-hashes', host)
        bb.utils.mkdirhier(dst)
        with open(os.path.join(dst, pkgver + '.sha256'), 'w') as f:
            f.write(sha)
}
addtask write_hosted_hashes after do_unpack before do_configure

do_pub_get_offline() {
    cd ${PUBSPEC_APP_DIR}
    dart pub get --offline --enforce-lockfile
}
addtask pub_get_offline after do_check_pubspec_lock do_write_hosted_hashes before do_compile
do_pub_get_offline[network] = "0"
