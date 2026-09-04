# SPDX-License-Identifier: MIT
"""A pub workspace member has no lockfile of its own.

The resolution lives at the workspace root and covers every member, so looking
in the app directory finds nothing and generates a per-app resolution -- the
wrong unit. Every app the Flutter SDK ships is like this: one lockfile at the
root for 78 members. See #867.
"""
import sys
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import create_recipes  # noqa: E402


def _workspace(tmp_path, member='examples/hello_world', declared=True):
    root = tmp_path / 'flutter'
    app = root / member
    app.mkdir(parents=True)
    members = f'  - {member}\n' if declared else '  - some/other\n'
    (root / 'pubspec.yaml').write_text(
        f'name: _flutter_packages\nworkspace:\n{members}')
    (root / 'pubspec.lock').write_text('packages: {}\n')
    (app / 'pubspec.yaml').write_text(
        'name: hello_world\nresolution: workspace\n')
    return root, app


def test_member_resolves_to_the_workspace_root(tmp_path):
    root, app = _workspace(tmp_path)
    assert create_recipes.workspace_root_for(str(app)) == str(root)


def test_nested_member_walks_all_the_way_up(tmp_path):
    root, app = _workspace(tmp_path, member='dev/integration_tests/ui')
    assert create_recipes.workspace_root_for(str(app)) == str(root)


def test_an_app_not_declaring_workspace_resolution_is_standalone(tmp_path):
    root, app = _workspace(tmp_path)
    (app / 'pubspec.yaml').write_text('name: hello_world\n')
    assert create_recipes.workspace_root_for(str(app)) is None


def test_a_root_that_does_not_list_the_app_is_not_its_workspace(tmp_path):
    # declaring `resolution: workspace` is not enough; the root has to agree
    root, app = _workspace(tmp_path, declared=False)
    assert create_recipes.workspace_root_for(str(app)) is None


def test_no_ancestor_pubspec_at_all(tmp_path):
    app = tmp_path / 'standalone'
    app.mkdir()
    (app / 'pubspec.yaml').write_text('name: x\nresolution: workspace\n')
    assert create_recipes.workspace_root_for(str(app)) is None


def test_a_missing_pubspec_is_not_a_crash(tmp_path):
    assert create_recipes.workspace_root_for(str(tmp_path / 'nope')) is None
