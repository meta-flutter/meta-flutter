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
#     passes offline, and asserts while it is there that every package the
#     fragment declared is staged where pub will look for it
#   * runs `flutter pub get --offline --enforce-lockfile` -- flutter rather
#     than dart, see the note above the task
#   * marks do_archive_pub_cache and do_restore_pub_cache noexec, so the
#     layer's own networked pub cache path stands down
#
# The recipe must set PUBSPEC_APP_DIR to the app source dir containing
# pubspec.yaml/pubspec.lock (default ${S}), and PUBSPEC_LOCK_FILE to the
# vendored lockfile it carries in SRC_URI.

PUB_CACHE_LOCAL ?= "pub_cache"
# The fragment stages the cache with SRC_URI subdir=, and subdir= is relative
# to UNPACKDIR, which is ${WORKDIR}/sources from scarthgap on and WORKDIR
# itself before that. Anchoring PUB_CACHE to WORKDIR unconditionally pointed
# it at a directory the packages were never unpacked into: they landed in
# ${WORKDIR}/sources/pub_cache while pub read ${WORKDIR}/pub_cache, which
# do_seed_pub_cache had filled from the SDK. Resolution then succeeded on
# whatever the SDK's own cache happened to carry and failed on the first
# package it did not -- with the vendored copy sitting unused one directory
# away. Take the same base do_install_pubspec_lock takes.
PUB_CACHE_BASE ?= "${@d.getVar('UNPACKDIR') or d.getVar('WORKDIR')}"
PUB_CACHE = "${PUB_CACHE_BASE}/${PUB_CACHE_LOCAL}"
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
    for root, dirs, files in os.walk(sdk_cache):
        # Skip pub's own metadata cache. It holds cached version listings and
        # advisory responses, not packages, and carrying them across is what
        # lets pub reach the network from an offline resolve:
        #
        #   hosted.dart:981  _versionInfo() returns null when offline and no
        #                    cached listing exists, so status() comes back
        #                    empty and advisoriesUpdated is null
        #   hosted.dart:774  _getAdvisories() returns immediately on a null
        #                    advisoriesUpdated -- but with a listing present it
        #                    proceeds, and has no isOffline guard of its own
        #
        # So a listing for a package turns an offline `pub get` into one that
        # will fetch https://pub.dev/api/packages/<pkg>/advisories whenever the
        # cached advisory response is older than the listing says it should be.
        # Without the listings that path cannot be entered at all.
        if '.cache' in dirs:
            dirs.remove('.cache')
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
    missing = []
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

        # Assert the package the fragment declared is actually where pub will
        # look for it. This loop already derives the exact path, so the check
        # is free -- and it is the one failure this class could not otherwise
        # see. do_seed_pub_cache fills the same tree from the SDK, so a
        # PUB_CACHE pointing somewhere the vendored packages were never
        # unpacked into does not come out empty; it comes out holding the
        # SDK's cache, and resolution gets as far as the first package the SDK
        # does not happen to ship. That reads as an ordinary dependency error
        # a long way from its cause, and every app whose dependencies the SDK
        # already carries would have passed while vendoring nothing.
        if not os.path.isdir(os.path.join(pub_cache, subdir[idx + 1:])):
            missing.append(pkgver)

    if missing:
        bb.fatal(
            "%d of the vendored packages are not staged under PUB_CACHE "
            "(%s): %s.\nSRC_URI subdir= unpacks into UNPACKDIR; check "
            "PUB_CACHE_BASE resolves to the same place."
            % (len(missing), pub_cache, ', '.join(sorted(missing)[:5])
               + (', ...' if len(missing) > 5 else '')))
}
addtask write_hosted_hashes after do_unpack do_patch before do_configure

# `flutter pub get`, not `dart pub get`. Both resolve offline from the staged
# cache, but only the flutter one writes .flutter-plugins-dependencies, which
# is what tells the build which plugins to register and feeds the generated
# dart_plugin_registrant.dart. With `dart pub get` an app builds green and
# registers no plugins.
# Resolving from a staged cache is sub-second work. A run that takes minutes
# is not doing the work slowly, it is waiting on something, and an unbounded
# wait inside a task that reports nothing until it ends is the worst shape for
# diagnosing that: the build looks alive for as long as it takes. Bound it, and
# let the timeout turn a stall into a failure with the verbose log attached.
PUB_GET_TIMEOUT ?= "600"

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

    # Keep pub's own output in a file rather than letting it into the task
    # log. bbfatal does not trigger bitbake's log dump, so anything written to
    # stdout here is lost on the failure that matters; the tail below is put on
    # the console deliberately instead.
    #
    # `|| rc=$?` and not `if ! ...`: after a negation $? is the status of the
    # negation, which is always 0, so the timeout could never be told apart
    # from an ordinary failure.
    pubget_log="${T}/pub_get_verbose.log"
    rc=0
    timeout ${PUB_GET_TIMEOUT} flutter --suppress-analytics pub get \
        --offline --enforce-lockfile --verbose > "$pubget_log" 2>&1 || rc=$?

    if [ $rc -ne 0 ]; then
        # Whether the task is actually network-isolated is worth recording
        # rather than assuming. bitbake-worker honours [network] = "0" through
        # bb.utils.to_boolean, but only applies it when bb.utils.is_local_uid()
        # holds, and it says so at debug level -- so in a container the isolation
        # can silently not happen. Probe DNS and TCP separately: a resolver with
        # nowhere to go is the classic source of a long, quiet stall.
        if timeout 5 getent hosts pub.dev >/dev/null 2>&1; then
            bbplain "pub-get: DNS resolves pub.dev"
        else
            bbplain "pub-get: DNS cannot resolve pub.dev"
        fi
        if timeout 5 sh -c 'exec 3<>/dev/tcp/151.101.1.140/443' >/dev/null 2>&1; then
            bbwarn "pub-get: the network is reachable in a task marked [network] = 0"
        else
            bbplain "pub-get: network unreachable, as intended"
        fi
        bbplain "pub-get: last 200 lines of pub --verbose"
        bbplain "$(tail -n 200 "$pubget_log" 2>/dev/null)"
        if [ $rc -eq 124 ]; then
            bbfatal "offline pub get did not finish within ${PUB_GET_TIMEOUT}s. Resolving from a staged cache takes under a second, so this is a wait rather than work; the tail above is where it stopped."
        fi
        bbfatal "offline pub get failed with exit code $rc"
    fi
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
