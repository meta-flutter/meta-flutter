# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# pub-cache.bbclass - consume a pubvendor.py-generated .inc
#
# Pairs with the SRC_URI fragment produced by tools/pubvendor/pubvendor.py:
#   * points PUB_CACHE at the staged tree
#   * installs PUBSPEC_LOCK_FILE, the lockfile the fragment was generated
#     from, over the app's own -- they differ, because the roll re-resolves
#     against the SDK this layer pins
#   * asserts the result matches PUBSPEC_LOCK_SHA256, which now catches the
#     layer's .inc and .lock drifting apart
#   * synthesizes hosted-hashes/<host>/<pkg>-<ver>.sha256 files from the
#     SRC_URI checksum flags, so newer Dart SDKs' content verification
#     passes offline
#   * runs `flutter pub get --offline --enforce-lockfile` -- flutter rather
#     than dart, see the note above the task
#   * marks do_archive_pub_cache and do_restore_pub_cache noexec, so the
#     layer's own networked pub cache path stands down
#
# The recipe must set PUBSPEC_APP_DIR to the app source dir containing
# pubspec.yaml/pubspec.lock (default ${S}), and PUBSPEC_LOCK_FILE to the
# vendored lockfile it carries in SRC_URI.

PUB_CACHE_LOCAL ?= "pub_cache"
PUB_CACHE = "${WORKDIR}/${PUB_CACHE_LOCAL}"
PUBSPEC_APP_DIR ?= "${S}"

export PUB_CACHE

# The fragment is generated from a lockfile resolved against the SDK this
# layer pins, which is not the one the app committed upstream. do_unpack
# brings that resolved lockfile in through SRC_URI; install it over the app's
# own before anything reads it, so what gets built is what was vendored.
python do_install_pubspec_lock() {
    import os
    import shutil

    name = d.getVar('PUBSPEC_LOCK_FILE')
    if not name:
        return
    # file:// unpacks into UNPACKDIR from scarthgap on, and straight into
    # WORKDIR before that. Take whichever this release provides.
    base = d.getVar('UNPACKDIR') or d.getVar('WORKDIR')
    src = os.path.join(base, name)
    if not os.path.isfile(src):
        bb.fatal("PUBSPEC_LOCK_FILE %s not found in %s; is it in SRC_URI?"
                 % (name, base))
    dst = os.path.join(d.getVar('PUBSPEC_APP_DIR'), 'pubspec.lock')
    shutil.copyfile(src, dst)
    bb.note("installed vendored pubspec.lock from %s" % name)
}
# after do_patch, not just do_unpack: a patch -- or a recipe task ordered
# around one, as with the user_defines injection in the appstream_dart app --
# can change pubspec.yaml or pubspec.lock. Resolving before that lands means
# resolving against source that is not what gets built, and an edit arriving
# afterwards makes pub re-resolve, which reaches for the network.
# Also after do_archive_pub_cache, for the same reason do_pub_get_offline is:
# a recipe that edits pubspec.yaml orders itself before that task, and this
# class makes it noexec without removing it from the graph. Installing the
# lock first leaves pubspec.yaml newer, which is one of the conditions pub
# uses to decide the lockfile is stale and re-resolve.
addtask install_pubspec_lock after do_unpack do_patch do_archive_pub_cache before do_check_pubspec_lock

# do_unpack stages the app's dependencies into PUB_CACHE, and nothing else.
# flutter's own tool packages are not among them: flutter_tools has its own
# pubspec -- it depends on `test`, among others -- and resolving that is the
# first thing `flutter pub get` does. Without them the tool goes to pub.dev
# and fails, reporting a package the app never mentions.
#
# common.inc gets these by copying the SDK's .pub-cache wholesale, but it
# rmtree's the destination first, which would discard everything do_unpack
# staged. Merge instead, and let the staged copies win: those came through
# SRC_URI with checksums bitbake verified.
python do_seed_pub_cache() {
    import os
    import shutil

    sdk_cache = os.path.join(d.getVar('FLUTTER_SDK'), '.pub-cache')
    if not os.path.isdir(sdk_cache):
        bb.fatal("no .pub-cache in %s; flutter-sdk-native did not stage one"
                 % d.getVar('FLUTTER_SDK'))
    dest = d.getVar('PUB_CACHE')

    added = 0
    for root, _, files in os.walk(sdk_cache):
        rel = os.path.relpath(root, sdk_cache)
        target = os.path.join(dest, rel) if rel != '.' else dest
        bb.utils.mkdirhier(target)
        for name in files:
            dst = os.path.join(target, name)
            if os.path.exists(dst):
                continue
            shutil.copy2(os.path.join(root, name), dst)
            added += 1
    bb.note("seeded %d files from the SDK pub cache" % added)
}
addtask seed_pub_cache after do_unpack do_patch before do_pub_get_offline

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
addtask check_pubspec_lock after do_install_pubspec_lock before do_configure

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
addtask write_hosted_hashes after do_unpack do_patch before do_configure

# `flutter pub get`, not `dart pub get`. Both resolve offline from the staged
# cache, but only the flutter one writes .flutter-plugins-dependencies, which
# is what tells the build which plugins to register and feeds the generated
# dart_plugin_registrant.dart. With `dart pub get` an app builds green and
# registers no plugins.
do_pub_get_offline() {
    # The SDK is staged by flutter-sdk-native and is not on the task PATH by
    # default. HOME and XDG_CONFIG_HOME are set for the same reasons
    # common.inc sets them for its own pub invocations: dart and flutter both
    # write into them and must not touch the builder's real home.
    export PATH="${FLUTTER_SDK}/bin:${PUB_CACHE}/bin:$PATH"
    export HOME="${WORKDIR}"
    export XDG_CONFIG_HOME="${WORKDIR}"

    # Opt out of analytics before resolving, while the config write is the
    # only thing happening. It is a local settings file, so it needs no
    # network itself, but leaving it unset makes the first flutter run in a
    # fresh XDG_CONFIG_HOME do its first-run work -- which does.
    flutter config --no-analytics --no-cli-animations >/dev/null 2>&1 || true

    cd ${PUBSPEC_APP_DIR}
    flutter --suppress-analytics pub get --offline --enforce-lockfile
}
# Ordered after do_archive_pub_cache as well, even though this class makes
# that task noexec. It is the layer's convention for "before pub runs": a
# recipe that must touch the source first orders itself before it, as the
# appstream_dart app does to inject its hooks section. Ordering only against
# do_patch left both siblings of it with no relative order, so the edit could
# land after resolution -- and pub then re-resolves, fetching security
# advisories from pub.dev and failing with no network.
addtask pub_get_offline after do_check_pubspec_lock do_write_hosted_hashes do_seed_pub_cache do_patch do_archive_pub_cache before do_compile
do_pub_get_offline[network] = "0"

# The layer's own pub cache path fetches with the network and carries the
# resolution artifacts through an archive (common.inc, the .project copy in
# do_archive_pub_cache and the moves back in do_restore_pub_cache). A recipe
# staging its cache through SRC_URI needs neither: do_unpack puts the packages
# in place and do_pub_get_offline regenerates the artifacts locally. Leaving
# both enabled would mean two populators of ${WORKDIR}/pub_cache, one of them
# reaching for the network, which is the thing this class exists to avoid.
#
# Doing it here rather than in common.inc keeps opting in to exactly
# "inherit pub-cache", with no effect on any recipe that does not.
do_archive_pub_cache[noexec] = "1"
do_restore_pub_cache[noexec] = "1"
