# SPDX-FileCopyrightText: (C) 2026 Joel Winarske
# SPDX-License-Identifier: MIT
#
# Each test is a defect that actually shipped, plus the case that must not be
# flagged. The negative half matters as much: a check that cries wolf gets
# routed around, and then it may as well not exist.

import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

import consistency  # noqa: E402


def _write(base, rel, text):
    p = base / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(text)
    return p


def test_a_patch_no_src_uri_names_is_reported(tmp_path):
    """0001-Add-missing-stdint-header.patch sat unreferenced on four branches.

    It was not dead weight -- it was a fix for a real compile error that
    everyone believed was applied.
    """
    _write(tmp_path, 'recipes-x/foo/files/0001-fix.patch', '--- a\n+++ b\n')
    _write(tmp_path, 'recipes-x/foo/foo_git.bb', 'SRC_URI = "git://example.com/foo"\n')
    assert consistency.unwired_patches(str(tmp_path)) == [
        'recipes-x/foo/files/0001-fix.patch']


def test_a_patch_in_src_uri_is_not_reported(tmp_path):
    _write(tmp_path, 'recipes-x/foo/files/0001-fix.patch', '--- a\n+++ b\n')
    _write(tmp_path, 'recipes-x/foo/foo_git.bb',
           'SRC_URI = "git://example.com/foo file://0001-fix.patch"\n')
    assert consistency.unwired_patches(str(tmp_path)) == []


def test_roll_input_patches_are_not_reported(tmp_path):
    """tools/patches/ is handed to the generator by directory, not by SRC_URI."""
    _write(tmp_path, 'tools/patches/someapp/0001-fix.patch', '--- a\n+++ b\n')
    assert consistency.unwired_patches(str(tmp_path)) == []


def test_destsuffix_on_an_http_url_is_reported(tmp_path):
    """The material fonts entry, verbatim in shape.

    destsuffix is implemented by git, hg, npm and npmsw. wget inherits the
    generic unpack, which honours subdir alone and ignores the parameter, so
    the file is fetched and left in WORKDIR.
    """
    _write(tmp_path, 'recipes-x/foo/foo_git.bb',
           'SRC_URI = "https://example.com/fonts.zip;name=fonts'
           ';destsuffix=${D}/usr/share/foo"\n')
    assert consistency.ineffective_destsuffix(str(tmp_path)) == [
        'recipes-x/foo/foo_git.bb:1']


def test_destsuffix_on_a_git_url_is_not_reported(tmp_path):
    _write(tmp_path, 'recipes-x/foo/foo_git.bb',
           'SRC_URI = "git://example.com/foo;destsuffix=src/foo"\n')
    assert consistency.ineffective_destsuffix(str(tmp_path)) == []


def test_packagegroup_naming_a_missing_recipe_of_its_family_is_reported(tmp_path):
    """packagegroup-playx-... named an app recipe removed months earlier.

    Nothing regenerated it -- there was no manifest entry -- so the group could
    not resolve for anyone who pulled it in.
    """
    _write(tmp_path, 'recipes-platform/packagegroups/packagegroup-playx-thing.bb',
           'RDEPENDS:${PN} += " \\\n    playx-thing-example \\\n    "\n')
    hits = consistency.dangling_packagegroup_rdepends(str(tmp_path))
    assert hits == ['recipes-platform/packagegroups/packagegroup-playx-thing.bb: '
                    'playx-thing-example']


def test_packagegroup_depending_on_another_layer_is_not_reported(tmp_path):
    """atk, cairo-dev and friends are not ours to resolve.

    An earlier draft flagged every one of them, which would have made the check
    useless on its first run.
    """
    _write(tmp_path, 'recipes-platform/packagegroups/packagegroup-foo-deps.bb',
           'RDEPENDS:${PN} += " \\\n    atk \\\n    cairo-dev \\\n    "\n')
    assert consistency.dangling_packagegroup_rdepends(str(tmp_path)) == []


def test_packagegroup_naming_a_recipe_that_exists_is_not_reported(tmp_path):
    _write(tmp_path, 'recipes-platform/packagegroups/packagegroup-foo-thing.bb',
           'RDEPENDS:${PN} += " foo-thing-example "\n')
    _write(tmp_path, 'recipes-x/foo/foo-thing-example_1.0.bb', 'SUMMARY = "x"\n')
    assert consistency.dangling_packagegroup_rdepends(str(tmp_path)) == []


def test_british_spelling_inside_an_identifier_is_reported(tmp_path):
    """A \\b prefix misses this, which is how five survived a sweep."""
    _write(tmp_path, 'tools/x.py', 'def test_an_unrecognised_failure():\n    pass\n')
    hits = consistency.british_spellings(str(tmp_path))
    assert len(hits) == 1 and 'unrecognis' in hits[0]


def test_override_style_reads_the_branch(tmp_path):
    _write(tmp_path, 'conf/include/a.inc', 'FOO:append = " x"\n')
    assert consistency.override_style(str(tmp_path)) == 'new'
    _write(tmp_path, 'conf/include/a.inc', 'FOO_append = " x"\n')
    assert consistency.override_style(str(tmp_path)) == 'old'


def test_override_style_is_undecidable_when_mixed(tmp_path):
    """Says nothing rather than guessing."""
    _write(tmp_path, 'conf/include/a.inc', 'FOO:append = " x"\nBAR_append = " y"\n')
    assert consistency.override_style(str(tmp_path)) is None


def test_license_operator_reads_the_generated_recipes(tmp_path):
    _write(tmp_path, 'a/x_1.0.bb', 'LICENSE = "MIT & Apache-2.0"\n')
    assert consistency.license_operator(str(tmp_path)) == '&'
    _write(tmp_path, 'a/x_1.0.bb', 'LICENSE = "MIT AND Apache-2.0"\n')
    assert consistency.license_operator(str(tmp_path)) == 'AND'
