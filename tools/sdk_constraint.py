#!/usr/bin/env python3
# SPDX-FileCopyrightText: (C) 2026 Joel Winarske
# SPDX-License-Identifier: MIT
#
# Does an app's declared SDK constraint admit the SDK this layer pins?
#
# Deliberately conservative. Every function returns None for anything it does
# not fully understand, and a None is never treated as an exclusion: a gate
# that misfires drops an app from the layer silently, which is worse than the
# build failure it was meant to prevent. See #850.

import os
import re

_VERSION = re.compile(
    r'^(\d+)\.(\d+)\.(\d+)'          # core
    r'(?:-([0-9A-Za-z.-]+))?'          # prerelease
    r'(?:\+[0-9A-Za-z.-]+)?$')         # build metadata, ignored
_TERM = re.compile(r'^(>=|<=|>|<|\^)?\s*(.+)$')


def _prerelease_key(part):
    # semver: numeric identifiers compare numerically and rank below
    # alphanumeric ones.
    return (0, int(part), '') if part.isdigit() else (1, 0, part)


def parse_version(text):
    """A sortable key for a version, or None if it is not one.

    Prereleases are handled rather than declined. The dominant constraint in
    real pubspecs is the form `^3.11.0-0` -- 132 of 139 in the Flutter SDK
    alone -- so refusing them leaves the gate inert on almost everything it
    would be asked about.

    The key sorts a release above its own prereleases, which is what semver
    says and what the `-0` idiom relies on: `^3.11.0-0` exists precisely to
    admit 3.11.0 prereleases, so 3.13.1 must compare greater than 3.11.0-0.
    """
    if not isinstance(text, str):
        return None
    m = _VERSION.match(text.strip())
    if not m:
        return None
    core = tuple(int(g) for g in m.groups()[:3])
    pre = m.group(4)
    if pre is None:
        return core + (1, ())
    return core + (0, tuple(_prerelease_key(p) for p in pre.split('.')))


def parse_constraint(spec):
    """[(op, version)] for a pub version constraint, or None if not understood.

    Handles the forms that actually appear in a pubspec environment: a list of
    comparators ('>=3.0.0 <4.0.0'), a caret ('^3.10.0'), a bare version, and
    'any'. Ranges with commas, '||' alternatives and prereleases are not
    understood, and say so.
    """
    if not isinstance(spec, str):
        return None
    spec = spec.strip()
    if not spec or spec == 'any':
        return []
    if '||' in spec or ',' in spec:
        return None

    terms = []
    for token in spec.split():
        m = _TERM.match(token)
        if not m:
            return None
        op, raw = m.group(1) or '', m.group(2)
        version = parse_version(raw)
        if version is None:
            return None
        if op == '^':
            # pub's caret: up to the next breaking version, which for a 0.x
            # release is the next minor rather than the next major. The upper
            # bound is exclusive and carries no prerelease, so nothing at or
            # above it qualifies.
            major, minor = version[0], version[1]
            core = (major + 1, 0, 0) if major else (0, minor + 1, 0)
            upper = core + (0, ())
            terms.append(('>=', version))
            terms.append(('<', upper))
        else:
            terms.append((op or '>=', version))
    return terms


def satisfies(version_text, spec):
    """True / False / None, where None means 'not understood, do not act'."""
    version = parse_version(version_text)
    terms = parse_constraint(spec)
    if version is None or terms is None:
        return None
    for op, bound in terms:
        if op == '>=' and not version >= bound:
            return False
        if op == '>' and not version > bound:
            return False
        if op == '<=' and not version <= bound:
            return False
        if op == '<' and not version < bound:
            return False
    return True


def excluded_reason(yaml_obj, flutter_version, dart_version):
    """Why the pinned SDK is excluded by this pubspec, or None if it is not.

    None covers both 'compatible' and 'cannot tell', which are the same thing
    as far as the caller is concerned: do not skip the app.
    """
    env = (yaml_obj or {}).get('environment')
    if not isinstance(env, dict):
        return None

    for key, pinned, label in (('sdk', dart_version, 'Dart'),
                               ('flutter', flutter_version, 'Flutter')):
        spec = env.get(key)
        if spec is None or pinned is None:
            continue
        if satisfies(pinned, spec) is False:
            return (f'needs {label} {spec}, '
                    f'this layer pins {label} {pinned}')
    return None


def pinned_versions(layer_root=None):
    """(flutter, dart) this layer pins, either possibly None.

    Read from the layer rather than passed in: roll_meta_flutter.py updates
    flutter-version.inc before rolling any repo, so the file is already current
    by the time recipes are generated.
    """
    import json

    if layer_root is None:
        layer_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    include = os.path.join(layer_root, 'conf', 'include')

    flutter = None
    try:
        with open(os.path.join(include, 'flutter-version.inc')) as f:
            for line in f:
                if 'FLUTTER_SDK_TAG' in line and '=' in line:
                    m = re.search(r'"([^"]+)"', line)
                    if m:
                        flutter = m.group(1)
                    break
    except OSError:
        return None, None
    if flutter in (None, 'AUTOINC'):
        return None, None

    dart = None
    try:
        with open(os.path.join(include, 'releases_linux.json')) as f:
            for release in json.load(f).get('releases', []):
                if release.get('version') == flutter:
                    dart = release.get('dart_sdk_version')
                    break
    except (OSError, ValueError):
        pass
    # Releases carry "3.13.2" or occasionally "3.13.2 (build 3.13.2-x.y.z)".
    if isinstance(dart, str) and ' ' in dart:
        dart = dart.split()[0]
    return flutter, dart
