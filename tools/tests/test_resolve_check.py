# SPDX-License-Identifier: MIT
"""Running pub to find out whether an app's dependencies actually solve.

The static gate reads declared constraints. This catches the case it cannot:
an app that declares a range the pinned SDK satisfies and still fails to
resolve because a dependency of a dependency does not. See #850 case (2).

Every test here is about the same risk: a roll must not drop an app because
pub.dev was briefly unreachable. Anything that is not clearly pub saying "this
cannot be satisfied" has to mean "do not skip".
"""
import os
import stat
import sys
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import create_recipes  # noqa: E402


@pytest.fixture
def flutter(tmp_path, monkeypatch):
    """A fake `flutter` whose output and exit code each test chooses."""
    bin_dir = tmp_path / 'bin'
    bin_dir.mkdir()
    shim = bin_dir / 'flutter'

    def program(stdout='', stderr='', rc=0):
        shim.write_text(
            '#!/bin/sh\n'
            f'cat <<\'OUT\'\n{stdout}\nOUT\n'
            f'cat >&2 <<\'ERR\'\n{stderr}\nERR\n'
            f'exit {rc}\n')
        shim.chmod(shim.stat().st_mode | stat.S_IEXEC)
    monkeypatch.setenv('PATH', str(bin_dir), prepend=os.pathsep)
    return program


def test_a_clean_resolve_is_not_a_reason(tmp_path, flutter):
    flutter(stdout='Got dependencies!', rc=0)
    assert create_recipes.resolve_check(str(tmp_path), 'app') is None


def test_version_solving_failure_is_a_reason(tmp_path, flutter):
    flutter(stderr='Because app depends on foo >=2.0.0 which requires SDK '
                   'version >=3.20.0, version solving failed.', rc=1)
    r = create_recipes.resolve_check(str(tmp_path), 'app')
    assert r is not None
    assert 'do not solve' in r
    assert 'Because app depends on foo' in r


@pytest.mark.parametrize('err', [
    'Got socket error trying to find package foo at https://pub.dev.',
    'SocketException: Failed host lookup: pub.dev',
    'Connection closed before full header was received',
    'pub get failed: connection timed out',
])
def test_a_network_failure_is_not_an_incompatibility(tmp_path, flutter, err):
    # dropping an app because pub.dev blinked would be worse than not checking
    flutter(stderr=err, rc=1)
    assert create_recipes.resolve_check(str(tmp_path), 'app') is None


def test_an_unrecognised_failure_is_not_an_incompatibility(tmp_path, flutter):
    flutter(stderr='something nobody has seen before', rc=1)
    assert create_recipes.resolve_check(str(tmp_path), 'app') is None


def test_no_flutter_on_path_is_not_an_incompatibility(tmp_path, monkeypatch):
    monkeypatch.setenv('PATH', str(tmp_path / 'empty'))
    assert create_recipes.resolve_check(str(tmp_path), 'app') is None


def test_a_hang_is_not_an_incompatibility(tmp_path, monkeypatch):
    bin_dir = tmp_path / 'bin'
    bin_dir.mkdir()
    shim = bin_dir / 'flutter'
    shim.write_text('#!/bin/sh\nsleep 30\n')
    shim.chmod(shim.stat().st_mode | stat.S_IEXEC)
    monkeypatch.setenv('PATH', str(bin_dir), prepend=os.pathsep)
    assert create_recipes.resolve_check(str(tmp_path), 'app', timeout=1) is None
