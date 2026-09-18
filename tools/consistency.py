#!/usr/bin/env python3
# SPDX-FileCopyrightText: (C) 2026 Joel Winarske
# SPDX-License-Identifier: MIT
#
# Layer consistency checks. See #967.
#
# Everything here is a defect found by hand that a grep would have caught, on
# more than one branch, sometimes for months. The checks are deliberately dumb:
# they read text, they do not parse bitbake, and a check that cannot decide
# says nothing rather than guessing. A false failure here costs more than the
# defect it was meant to catch, because the next person routes around the
# script.

import os
import re

# Fetchers that implement destsuffix. wget defines no unpack() and inherits the
# generic one, which honours subdir alone and ignores an unknown parameter --
# so destsuffix on an http(s) URL places nothing and raises no error.
_DESTSUFFIX_FETCHERS = ('git://', 'gitsm://', 'hg://', 'npm://', 'npmsw://')

_RECIPE_SUFFIX = ('.bb', '.bbappend', '.inc', '.bbclass')

_BRITISH = re.compile(
    r'(unrecognis|recognis|behaviour|initialis|organis|optimis|normalis'
    r'|serialis|colour|licence|centre|labelled|modelled|travelling)',
    re.IGNORECASE)


def _walk(root, suffixes):
    for base, dirs, files in os.walk(root):
        dirs[:] = [d for d in dirs if d != '.git']
        for name in files:
            if name.endswith(suffixes):
                yield os.path.join(base, name)


def _read(path):
    with open(path, 'r', errors='replace') as f:
        return f.read()


def _recipe_text(root):
    """Every recipe-ish file's text, concatenated. Enough for substring checks."""
    return '\n'.join(_read(p) for p in _walk(root, _RECIPE_SUFFIX))


def unwired_patches(root):
    """Patch files no SRC_URI names.

    An unreferenced patch is either dead weight or -- worse -- a fix someone
    believes is applied. Both orphans found in this layer were the second kind:
    0001-Add-missing-stdint-header.patch on four branches and
    0001-suppres-musl-libc-warning.patch on three, neither in any SRC_URI.
    """
    text = _recipe_text(root)
    missing = []
    for path in _walk(root, ('.patch', '.diff')):
        # only a recipe's own files/ dir follows the SRC_URI convention.
        # tools/patches/ holds roll inputs, handed to the generator by
        # directory through patch_dir and named in the recipes it emits.
        if os.sep + 'files' + os.sep not in path:
            continue
        name = os.path.basename(path)
        if name not in text:
            missing.append(os.path.relpath(path, root))
    return sorted(missing)


def ineffective_destsuffix(root):
    """destsuffix= on a fetcher that ignores it.

    The material fonts entry carried
    destsuffix=${D}...fonts.zip on an https:// URL for as long as it existed,
    placed nothing, and was masked by flutter_tools downloading the same file.
    """
    bad = []
    for path in _walk(root, _RECIPE_SUFFIX):
        for n, line in enumerate(_read(path).splitlines(), 1):
            if 'destsuffix=' not in line:
                continue
            if any(f in line for f in _DESTSUFFIX_FETCHERS):
                continue
            # a continuation may carry the parameter without the scheme; only
            # flag a line that clearly holds a URL of its own
            if '://' not in line:
                continue
            bad.append('%s:%d' % (os.path.relpath(path, root), n))
    return sorted(bad)


def british_spellings(root, suffixes=None):
    """US English, including inside identifiers.

    A \\b prefix misses test_an_unrecognised_failure, which is how five of
    these survived a sweep.
    """
    suffixes = suffixes or (_RECIPE_SUFFIX + ('.py', '.md', '.yml', '.yaml'))
    hits = []
    for path in _walk(root, suffixes):
        # this file and its tests spell the words on purpose
        if os.path.basename(path) in ('consistency.py', 'test_consistency.py'):
            continue
        for n, line in enumerate(_read(path).splitlines(), 1):
            m = _BRITISH.search(line)
            if m:
                hits.append('%s:%d: %s' % (os.path.relpath(path, root), n, m.group(0)))
    return sorted(hits)


def _declared_recipe_names(root):
    names = set()
    for path in _walk(root, ('.bb',)):
        names.add(os.path.basename(path).split('_')[0].replace('.bb', ''))
    return names


def dangling_packagegroup_rdepends(root):
    """packagegroup RDEPENDS naming a recipe that is not in the layer.

    packagegroup-playx-flutter-playx-3d-scene had exactly one entry,
    playx-flutter-playx-3d-scene-example-my-fox-example, which had not existed
    since the recipes were removed. It could not resolve for anyone who pulled
    it in.

    Only packagegroups are checked, and only against recipes this layer
    provides: a normal recipe may legitimately RDEPEND on another layer.
    """
    provided = _declared_recipe_names(root)
    dangling = []
    for path in _walk(root, ('.bb',)):
        name = os.path.basename(path)
        if not name.startswith('packagegroup-'):
            continue
        # A packagegroup RDEPENDs freely on other layers -- atk, cairo-dev and
        # the rest are not ours to resolve. Only its own family is checkable:
        # packagegroup-<stem> naming <stem>-something. That is the shape the
        # playx one had, and it is the shape a removed app recipe leaves
        # behind.
        stem = name[len('packagegroup-'):].split('_')[0].replace('.bb', '')
        text = _read(path)
        for m in re.finditer(r'RDEPENDS[:_]\$\{PN\}[^=]*=\s*"(.*?)"', text, re.S):
            for dep in m.group(1).split():
                if dep in ('\\', '') or dep.startswith('$'):
                    continue
                if not dep.startswith(stem):
                    continue
                if dep not in provided:
                    dangling.append('%s: %s' % (os.path.relpath(path, root), dep))
    return sorted(set(dangling))


def override_style(root):
    """Which override syntax this branch's own metadata uses.

    Returns 'new', 'old', or None when the evidence is mixed or absent --
    common.py's OVERRIDE_STYLE should agree with it. dunfell is the branch that
    needs 'old'; carrying 'new' there emits fragments its bitbake cannot parse.
    """
    new = old = 0
    for sub in ('conf', 'classes'):
        d = os.path.join(root, sub)
        if not os.path.isdir(d):
            continue
        for path in _walk(d, _RECIPE_SUFFIX):
            text = _read(path)
            new += len(re.findall(r'^[A-Z][A-Za-z_]*:(?:append|prepend|remove)\b',
                                  text, re.M))
            old += len(re.findall(r'^[A-Z][A-Za-z_]*_(?:append|prepend|remove)\b',
                                  text, re.M))
    if new and not old:
        return 'new'
    if old and not new:
        return 'old'
    return None


def license_operator(root):
    """Which license operator this branch's generated recipes use.

    '&' before oe-core 51c7930220, 'AND' after. common.py declares it because
    it is correct on one branch and a regression on the other.
    """
    amp = land = 0
    for path in _walk(root, ('.bb',)):
        for m in re.finditer(r'^LICENSE\s*=\s*"([^"]*)"', _read(path), re.M):
            value = m.group(1)
            amp += value.count(' & ')
            land += len(re.findall(r'\bAND\b', value))
    if amp and not land:
        return '&'
    if land and not amp:
        return 'AND'
    return None


CHECKS = (
    ('unwired patches', unwired_patches,
     'patch file no SRC_URI names -- dead, or a fix believed to be applied'),
    ('ineffective destsuffix', ineffective_destsuffix,
     'destsuffix on a fetcher that ignores it; the file is not placed'),
    ('dangling packagegroup RDEPENDS', dangling_packagegroup_rdepends,
     'packagegroup names a recipe of its own family that is not in the layer'),
    ('british spellings', british_spellings,
     'US English, identifiers included'),
)


def main(argv=None):
    import argparse

    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument('--path', default='.', help='layer root')
    ap.add_argument('--constants', action='store_true',
                    help='also report the branch constants common.py should declare')
    args = ap.parse_args(argv)

    failed = 0
    for name, fn, why in CHECKS:
        hits = fn(args.path)
        if not hits:
            print('ok   %s' % name)
            continue
        failed += 1
        print('FAIL %s -- %s' % (name, why))
        for h in hits:
            print('       %s' % h)

    if args.constants:
        print('\nbranch constants, from this branch\'s own metadata:')
        print('  OVERRIDE_STYLE    %s' % (override_style(args.path) or 'undecidable'))
        print('  LICENSE_OPERATOR  %s' % (license_operator(args.path) or 'undecidable'))
        print('  common.py must agree; each is a regression on the other branch.')

    return 1 if failed else 0


if __name__ == '__main__':
    raise SystemExit(main())
