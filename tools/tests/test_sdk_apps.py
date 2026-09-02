# SPDX-License-Identifier: MIT
"""Selecting which Flutter SDK apps become recipes.

The risk here is the same one #850 warns about: a filter that misfires drops an
app silently. Every rejection rule is therefore tested from both sides -- that
it rejects what it should, and that it leaves alone the things that merely look
like what it rejects.
"""
import sys
from pathlib import Path

import pytest
import yaml

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import sdk_apps  # noqa: E402


def _app(root, rel, *, main=True, linux=True, spec=None):
    d = root / rel
    d.mkdir(parents=True, exist_ok=True)
    (d / 'pubspec.yaml').write_text(yaml.safe_dump(
        spec or {'name': rel.rsplit('/', 1)[-1], 'environment': {'sdk': '^3.11.0-0'}}))
    if main:
        (d / 'lib').mkdir(exist_ok=True)
        (d / 'lib' / 'main.dart').write_text('void main() {}\n')
    if linux:
        (d / 'linux').mkdir(exist_ok=True)
    return d


def _find(root):
    return sdk_apps.find_apps(str(root), '3.47.1', '3.13.1',
                              lambda p: yaml.safe_load(open(p)))


@pytest.mark.parametrize('rel', [
    'dev/integration_tests/android_views',            # prefix
    'dev/integration_tests/hybrid_android_views',     # infix
    'dev/integration_tests/ios_app_with_extensions',
    'dev/integration_tests/windows_startup_test',
    'dev/integration_tests/web',                      # whole component
])
def test_platform_specific_apps_are_rejected(tmp_path, rel):
    _app(tmp_path, rel)
    cands, rejected = _find(tmp_path)
    assert cands == []
    assert rejected == [(rel, 'platform-specific')]


def test_a_platform_name_as_a_suffix_is_not_platform_specific(tmp_path):
    # examples/multiple_windows is about multiple application windows. A naive
    # pattern matches its `_windows` suffix and drops a legitimate app; that is
    # the misfire this whole module has to avoid, so it is pinned here.
    _app(tmp_path, 'examples/multiple_windows')
    cands, rejected = _find(tmp_path)
    assert [c['path'] for c in cands] == ['examples/multiple_windows']
    assert rejected == []


def test_windowing_test_is_not_windows(tmp_path):
    _app(tmp_path, 'dev/integration_tests/windowing_test')
    cands, _ = _find(tmp_path)
    assert [c['path'] for c in cands] == ['dev/integration_tests/windowing_test']


@pytest.mark.parametrize('rel', ['dev/bots', 'dev/devicelab', 'dev/automated_tests'])
def test_tooling_is_rejected(tmp_path, rel):
    _app(tmp_path, rel)
    _, rejected = _find(tmp_path)
    assert rejected == [(rel, 'tooling, not an app')]


def test_a_package_without_an_entry_point_is_not_an_app(tmp_path):
    _app(tmp_path, 'packages/flutter_tools', main=False)
    cands, rejected = _find(tmp_path)
    assert cands == []
    assert rejected == [('packages/flutter_tools', 'no lib/main.dart')]


def test_an_app_without_a_linux_runner_is_rejected(tmp_path):
    _app(tmp_path, 'examples/splash', linux=False)
    _, rejected = _find(tmp_path)
    assert rejected == [('examples/splash', 'no linux runner')]


def test_an_app_the_pinned_sdk_cannot_satisfy_is_rejected(tmp_path):
    _app(tmp_path, 'examples/future', spec={
        'name': 'future', 'environment': {'sdk': '>=99.0.0 <100.0.0'}})
    _, rejected = _find(tmp_path)
    assert len(rejected) == 1
    assert 'Dart' in rejected[0][1] and '3.13.1' in rejected[0][1]


def test_rejections_carry_a_reason(tmp_path):
    # an app vanishing with no trace is the failure worth avoiding
    _app(tmp_path, 'dev/integration_tests/android_views')
    _, rejected = _find(tmp_path)
    assert all(isinstance(r, str) and r for _, r in rejected)


def test_recipe_name():
    assert sdk_apps.recipe_name('examples/hello_world') == 'flutter-sdk-examples-hello-world'
    assert sdk_apps.recipe_name('dev/integration_tests/ui') == 'flutter-sdk-dev-integration-tests-ui'


def test_render_matches_the_hand_written_recipe(tmp_path):
    app = {'path': 'examples/hello_world', 'name': 'hello_world', 'description': 'x'}
    out = sdk_apps.render(app, {'examples/hello_world': {
        'summary': 'hello_world',
        'description': "The smallest app the Flutter SDK ships: a single Text widget."}})
    assert 'PUBSPEC_APPNAME = "hello_world"' in out
    assert 'FLUTTER_APPLICATION_PATH = "examples/hello_world"' in out
    assert 'require conf/include/flutter-sdk-app.inc' in out
    assert 'RDEPENDS' not in out


def test_overrides_survive_generation(tmp_path):
    # hand-tuned runtime dependencies must not be lost on the next roll
    app = {'path': 'dev/integration_tests/flutter_gallery',
           'name': 'flutter_gallery', 'description': ''}
    out = sdk_apps.render(app, {'dev/integration_tests/flutter_gallery': {
        'rdepends': ['xdg-user-dirs']}})
    assert 'RDEPENDS:${PN} += "xdg-user-dirs"' in out


def test_apps_are_emitted_in_a_stable_order(tmp_path):
    for rel in ['examples/zebra', 'examples/alpha', 'dev/manual_tests']:
        _app(tmp_path, rel)
    cands, _ = _find(tmp_path)
    assert [c['path'] for c in cands] == sorted(c['path'] for c in cands)


def test_roll_sdk_apps_is_not_fatal_when_the_clone_fails(tmp_path, monkeypatch, capsys):
    """A roll that has already bumped the SDK must not be thrown away.

    But it has to say so: recipes left describing the previous SDK keep
    parsing, and only the handful built in CI would ever notice.
    """
    import subprocess as sp
    monkeypatch.setattr(sdk_apps, 'pinned_release_hash', lambda root: 'deadbeef')

    def boom(*a, **kw):
        raise sp.CalledProcessError(128, 'git', stderr='could not read from remote')
    monkeypatch.setattr(sdk_apps, 'clone_pinned', boom)

    assert sdk_apps.roll_sdk_apps(str(tmp_path)) is False
    out = capsys.readouterr().out
    assert 'WARNING' in out
    assert 'describe an older SDK' in out


def test_roll_sdk_apps_reports_a_missing_release_hash(tmp_path, monkeypatch, capsys):
    monkeypatch.setattr(sdk_apps, 'pinned_release_hash', lambda root: None)
    assert sdk_apps.roll_sdk_apps(str(tmp_path)) is False
    assert 'no release hash' in capsys.readouterr().out


def test_roll_sdk_apps_generates_from_an_existing_clone(tmp_path):
    clone = tmp_path / 'flutter'
    _app(clone, 'examples/hello_world')
    (clone / 'pubspec.yaml').write_text('name: _flutter_packages\n')
    layer = tmp_path / 'layer'
    (layer / 'conf' / 'include').mkdir(parents=True)
    (layer / 'conf' / 'include' / 'flutter-version.inc').write_text(
        'FLUTTER_SDK_TAG ??= "3.47.1"\n')
    (layer / 'conf' / 'include' / 'releases_linux.json').write_text(
        '{"releases": [{"version": "3.47.1", "dart_sdk_version": "3.13.1"}]}')

    assert sdk_apps.roll_sdk_apps(str(layer), str(clone)) is True
    written = sorted(p.name for p in
                     (layer / 'recipes-graphics' / 'flutter-sdk' / 'apps').iterdir())
    assert 'flutter-sdk-examples-hello-world.bb' in written
