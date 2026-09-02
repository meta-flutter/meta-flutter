#!/usr/bin/env python3
# SPDX-FileCopyrightText: (C) 2026 Joel Winarske
# SPDX-License-Identifier: MIT
#
# Generate recipes for the apps that ship inside the Flutter SDK.
#
# These have no repository of their own: their source is the SDK this layer
# already pins, so there is no manifest entry to track and no SRCREV to bump.
# What the roll needs is only the list of apps, which it takes from a clone of
# flutter/flutter at the commit the pinned release names.
#
# Recipes are generated for every candidate; only a couple are built in CI.
# That is not waste -- a recipe that exists is parsed on every build of the
# layer, on every branch, so a generated recipe that does not parse breaks
# everything immediately. Parse coverage scales; build coverage does not.

import json
import os
import re
import shutil
import subprocess

import sdk_constraint

FLUTTER_REPO = 'https://github.com/flutter/flutter.git'

# Trees the SDK keeps its apps in. packages/ comes along because the workspace
# root lists members there and pub needs them to exist.
SPARSE_PATHS = ('examples', 'dev', 'packages')

# Paths that name a platform we do not build for. An app under one of these is
# not a candidate however well formed it is.
#
# A platform name counts as a prefix of a path component (android_views), an
# infix between underscores (hybrid_android_views), or a whole component (web).
# It deliberately does not count as a suffix: examples/multiple_windows is
# about multiple application windows, and matching it would silently drop a
# legitimate app -- exactly the misfire this kind of gate has to avoid.
_PLATFORMS = 'android|ios|macos|windows|web'
_PLATFORM_SPECIFIC = re.compile(
    rf'(?:^|[/_])(?:{_PLATFORMS})_'
    rf'|(?:^|/)(?:{_PLATFORMS})(?:/|$)')

# Trees that hold tooling rather than apps.
_TOOLING = ('dev/bots', 'dev/devicelab', 'dev/automated_tests',
            'dev/customer_testing', 'dev/forbidden_from_release_tests')


def pinned_release_hash(layer_root):
    """The flutter/flutter commit the pinned release was built from."""
    path = os.path.join(layer_root, 'conf', 'include', 'releases_linux.json')
    with open(path) as f:
        feed = json.load(f)
    tag = sdk_constraint.pinned_versions(layer_root)[0]
    for release in feed.get('releases', []):
        if release.get('version') == tag:
            return release.get('hash')
    return None


def clone_pinned(release_hash, dest):
    """A sparse, blobless checkout of flutter/flutter at one commit.

    A full checkout is about a gigabyte and the roll needs three directories of
    it. Filtering blobs and narrowing the checkout brings that to roughly 130 MB
    and a second, which is what makes cloning the right answer rather than
    keeping a hand-maintained list that rots silently.
    """
    if os.path.exists(dest):
        shutil.rmtree(dest)
    run = lambda *a, **kw: subprocess.run(a, check=True, cwd=kw.get('cwd'),
                                          capture_output=True, text=True)
    run('git', 'clone', '--filter=blob:none', '--no-checkout', '--depth', '1',
        FLUTTER_REPO, dest)
    run('git', 'sparse-checkout', 'init', '--cone', cwd=dest)
    run('git', 'sparse-checkout', 'set', *SPARSE_PATHS, cwd=dest)
    run('git', 'fetch', '--filter=blob:none', '--depth', '1', 'origin',
        release_hash, cwd=dest)
    run('git', 'checkout', release_hash, cwd=dest)
    return dest


def is_candidate(root, rel, spec, flutter_version, dart_version):
    """Why this app is not a candidate, or None if it is.

    Returning the reason rather than a bool so the roll can say why an app it
    found did not become a recipe -- an app disappearing with no trace is the
    thing to avoid.
    """
    if not os.path.isfile(os.path.join(root, rel, 'lib', 'main.dart')):
        return 'no lib/main.dart'
    if rel.startswith(_TOOLING):
        return 'tooling, not an app'
    if _PLATFORM_SPECIFIC.search(rel):
        return 'platform-specific'
    if not os.path.isdir(os.path.join(root, rel, 'linux')):
        return 'no linux runner'
    excluded = sdk_constraint.excluded_reason(spec, flutter_version, dart_version)
    if excluded:
        return excluded
    return None


def find_apps(root, flutter_version, dart_version, read_yaml):
    """(candidates, rejected) for the SDK checked out at root."""
    candidates, rejected = [], []
    for base in SPARSE_PATHS:
        for dirpath, dirs, files in os.walk(os.path.join(root, base)):
            dirs[:] = [d for d in dirs if not d.startswith('.')]
            if 'pubspec.yaml' not in files:
                continue
            rel = os.path.relpath(dirpath, root)
            spec = read_yaml(os.path.join(dirpath, 'pubspec.yaml'))
            if not spec or not spec.get('name'):
                continue
            reason = is_candidate(root, rel, spec, flutter_version, dart_version)
            if reason:
                rejected.append((rel, reason))
            else:
                candidates.append({'path': rel, 'name': spec['name'],
                                   'description': spec.get('description') or ''})
    candidates.sort(key=lambda a: a['path'])
    rejected.sort()
    return candidates, rejected


def recipe_name(app_path):
    return 'flutter-sdk-' + app_path.replace('/', '-').replace('_', '-')


def render(app, overrides):
    """The .bb for one app. Everything shared lives in flutter-sdk-app.inc."""
    extra = overrides.get(app['path'], {})
    summary = extra.get('summary') or app['name']
    description = extra.get('description') or app['description'] \
        or f"The Flutter SDK's {app['name']} example application."
    lines = [
        '#',
        '# Copyright (c) 2026 Joel Winarske',
        '#',
        '# SPDX-License-Identifier: MIT',
        '#',
        '# Generated by tools/sdk_apps.py. Edit the overrides file, not this.',
        '#',
        '',
        f'SUMMARY = "{summary}"',
        f'DESCRIPTION = "{description}"',
        '',
        f'PUBSPEC_APPNAME = "{app["name"]}"',
        f'FLUTTER_APPLICATION_PATH = "{app["path"]}"',
    ]
    rdepends = extra.get('rdepends') or []
    if rdepends:
        lines += ['', f'RDEPENDS:${{PN}} += "{" ".join(rdepends)}"']
    lines += ['', 'require conf/include/flutter-sdk-app.inc', '']
    return '\n'.join(lines)


def generate_workspace_fragment(clone_root, output_path):
    """Vendor the SDK workspace's pub cache into one SRC_URI fragment.

    SDK apps are pub workspace members: there is no per-app lockfile, only one
    at the workspace root covering every member. So unlike the third-party apps
    -- where vendoring costs a fragment each -- one fragment serves all of
    them, and the cost does not grow with the number of recipes.

    The lockfile is shipped in the release archive and `flutter update-packages`
    during the SDK build leaves it byte-identical, so this is the project's own
    resolution rather than one the roll invented.
    """
    import sys as _sys

    _sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)),
                                     'pubvendor'))
    import pubvendor
    from common import OVERRIDE_STYLE

    lock = os.path.join(clone_root, 'pubspec.lock')
    if not os.path.isfile(lock):
        print(f'WARNING: no workspace lockfile at {lock}; no fragment written')
        return None
    rc = pubvendor.main(['-i', lock, '-o', output_path,
                         '--style', OVERRIDE_STYLE,
                         '--provenance',
                         "the SDK's own, as shipped in the release archive "
                         "(pub workspace, all members)"])
    if rc != 0:
        print('WARNING: pubvendor failed on the workspace lockfile')
        return None
    return output_path


def generate(layer_root, clone_root, overrides_path, output_dir, read_yaml):
    """Write a recipe per candidate app. Returns (written, rejected)."""
    flutter, dart = sdk_constraint.pinned_versions(layer_root)
    overrides = {}
    if os.path.isfile(overrides_path):
        with open(overrides_path) as f:
            overrides = {k: v for k, v in json.load(f).items()
                         if not k.startswith('_')}

    candidates, rejected = find_apps(clone_root, flutter, dart, read_yaml)
    os.makedirs(output_dir, exist_ok=True)

    # Generated recipes are owned by this script: remove the ones it wrote last
    # time so an app that leaves the SDK leaves the layer with it.
    for existing in os.listdir(output_dir):
        if existing.startswith('flutter-sdk-') and \
                existing.endswith(('.bb', '-pubcache.inc')):
            os.remove(os.path.join(output_dir, existing))

    written = []
    for app in candidates:
        path = os.path.join(output_dir, recipe_name(app['path']) + '.bb')
        with open(path, 'w') as f:
            f.write(render(app, overrides))
        written.append(os.path.basename(path))

    fragment = os.path.join(output_dir, 'flutter-sdk-workspace-pubcache.inc')
    if generate_workspace_fragment(clone_root, fragment):
        written.append(os.path.basename(fragment))

    stale = sorted(set(overrides) - {a['path'] for a in candidates})
    if stale:
        print(f'NOTE: overrides name {len(stale)} app(s) that are not '
              f'candidates: {", ".join(stale)}')
    return written, rejected


def roll_sdk_apps(layer_root, clone=None):
    """Regenerate the SDK app recipes. Called by the roll; returns True on success.

    Not fatal when it cannot run. A roll that reaches this point has already
    updated the pinned SDK and the dart-sdk recipe, and one unavailable clone
    should not throw that away -- but it says so loudly, because recipes left
    describing the previous SDK would keep parsing and only the handful built
    in CI would notice.
    """
    import tempfile

    import yaml

    output_dir = os.path.join(layer_root, 'recipes-graphics', 'flutter-sdk', 'apps')
    overrides = os.path.join(output_dir, 'sdk-apps-overrides.json')

    tmp = None
    if clone is None:
        release_hash = pinned_release_hash(layer_root)
        if not release_hash:
            print('WARNING: no release hash for the pinned SDK; '
                  'SDK app recipes not regenerated and now describe an older SDK')
            return False
        tmp = tempfile.mkdtemp(prefix='flutter-sdk-apps-')
        try:
            clone = clone_pinned(release_hash, os.path.join(tmp, 'flutter'))
        except subprocess.CalledProcessError as e:
            shutil.rmtree(tmp, ignore_errors=True)
            print(f'WARNING: could not clone flutter/flutter at {release_hash}: '
                  f'{e.stderr.strip()[:200]}\n'
                  f'SDK app recipes not regenerated and now describe an older SDK')
            return False

    try:
        written, rejected = generate(layer_root, clone, overrides, output_dir,
                                     lambda p: yaml.safe_load(open(p)))
    finally:
        if tmp:
            shutil.rmtree(tmp, ignore_errors=True)

    print(f'{len(written)} SDK app recipe(s) written')
    print(f'{len(rejected)} app(s) not generated:')
    for rel, reason in rejected:
        print(f'  {rel}: {reason}')
    return True


def main(argv=None):
    import argparse

    ap = argparse.ArgumentParser(
        description="Generate recipes for the apps inside the Flutter SDK")
    ap.add_argument('--path', default='.', help='meta-flutter root')
    ap.add_argument('--clone', default=None,
                    help='an existing flutter/flutter checkout to use instead '
                         'of cloning (must be at the pinned commit)')
    args = ap.parse_args(argv)
    return 0 if roll_sdk_apps(args.path, args.clone) else 1


if __name__ == '__main__':
    import sys as _sys
    _sys.exit(main())
