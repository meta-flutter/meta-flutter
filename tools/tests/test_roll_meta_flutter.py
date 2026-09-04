# SPDX-License-Identifier: MIT
"""The roll has to be able to fail.

get_workspace_repos() runs one get_repo() per manifest entry in a thread pool.
A ThreadPoolExecutor captures a worker's exception in its Future and re-raises
it only on result(), so a roll that never read its futures exited 0 no matter
what happened inside -- a failed clone, a license mismatch, a manifest entry it
could not use. See #863.
"""
import sys
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import roll_meta_flutter as roll  # noqa: E402


def _repos(n):
    return [{'uri': f'https://example.invalid/{i}.git', 'branch': 'main'}
            for i in range(n)]


def _run(monkeypatch, behaviour):
    monkeypatch.setattr(roll, 'get_repo', behaviour)
    roll.get_workspace_repos('/nonexistent', _repos(4), '/out', '/pkg', None)


def test_all_succeeding_returns(monkeypatch):
    _run(monkeypatch, lambda **kw: None)


def test_one_worker_raising_fails_the_roll(monkeypatch, capsys):
    def one_bad(**kw):
        if kw['uri'].endswith('2.git'):
            raise roll.RollError('license_type does not match its source')

    with pytest.raises(SystemExit) as e:
        _run(monkeypatch, one_bad)
    assert e.value.code == 1
    out = capsys.readouterr().out
    assert '1 of 4 repos failed to roll' in out
    assert 'https://example.invalid/2.git' in out
    assert 'license_type does not match its source' in out


def test_every_failure_is_reported_not_just_the_first(monkeypatch, capsys):
    def all_bad(**kw):
        raise RuntimeError(f'boom {kw["uri"]}')

    with pytest.raises(SystemExit):
        _run(monkeypatch, all_bad)
    out = capsys.readouterr().out
    assert '4 of 4 repos failed to roll' in out
    for i in range(4):
        assert f'https://example.invalid/{i}.git' in out


def test_a_worker_calling_sys_exit_still_fails_the_roll(monkeypatch):
    # SystemExit in a worker thread ends that thread and nothing else, which is
    # why get_repo() raises instead. Catching BaseException means the old form
    # would not slip through either.
    def bad(**kw):
        if kw['uri'].endswith('0.git'):
            sys.exit(1)

    with pytest.raises(SystemExit) as e:
        _run(monkeypatch, bad)
    assert e.value.code == 1


@pytest.mark.parametrize('entry, missing', [
    ({'uri': None, 'branch': 'main'}, 'uri'),
    ({'uri': 'https://example.invalid/x.git', 'branch': None}, 'branch'),
])
def test_manifest_entry_missing_a_key_is_an_error(entry, missing):
    # These used to print "Skipping" and return, which is how the
    # flutter/flutter entry sat branchless in flutter-apps.json generating
    # nothing on every roll without anyone noticing.
    with pytest.raises(roll.RollError) as e:
        roll.get_repo(repo_path='/nonexistent', output_path='/out',
                      uri=entry['uri'], branch=entry['branch'], rev=None,
                      license_file=None, license_type='CLOSED',
                      validate_license=False, author=None, recipe_folder=None,
                      package_output_path='/pkg', ignore_list=None,
                      rdepends_list=None, output_path_override_list=None,
                      compiler_requires_network_list=None, src_folder=None,
                      src_files=None, entry_files=None, variables=None,
                      patch_dir=None)
    assert missing in str(e.value)
