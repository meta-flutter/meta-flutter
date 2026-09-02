# SPDX-License-Identifier: MIT
"""A gate that misfires drops an app from the layer silently.

That is worse than the build failure it prevents, so everything here is
checked from both directions: it excludes what it should, and it declines to
act on anything it does not fully understand. See #850.
"""
import sys
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import sdk_constraint as sc  # noqa: E402


@pytest.mark.parametrize('text, want', [
    # a sortable key, not a bare triple: the trailing 1 marks a release, which
    # is what makes it sort above its own prereleases
    ('3.13.1', (3, 13, 1, 1, ())),
    ('  3.0.0 ', (3, 0, 0, 1, ())),
    ('3.13', None),
    ('', None),
    (None, None),
])
def test_parse_version(text, want):
    assert sc.parse_version(text) == want


@pytest.mark.parametrize('spec, version, want', [
    ('>=3.0.0 <4.0.0', '3.13.1', True),
    ('>=3.0.0 <4.0.0', '4.0.0', False),
    ('>=3.14.0 <4.0.0', '3.13.1', False),
    ('^3.10.1', '3.13.1', True),
    ('^3.10.1', '4.0.0', False),
    ('^3.10.1', '3.10.0', False),
    ('^0.1.2', '0.1.9', True),
    ('^0.1.2', '0.2.0', False),   # 0.x caret stops at the next minor
    ('any', '3.13.1', True),
    ('', '3.13.1', True),
    ('>=3.10.0', '3.47.1', True),
    # The dominant real-world form: `-0` admits prereleases of the lower
    # bound, and a release must still compare above it. 132 of the 139
    # pubspecs in the Flutter SDK are written this way, so a parser that
    # declines them is a gate that never fires.
    ('^3.11.0-0', '3.13.1', True),
    ('>=3.5.0-0 <4.0.0', '3.13.1', True),
    ('^4.0.0-0', '3.13.1', False),
    ('^3.11.0-0', '3.11.0', True),
    ('^3.11.0-0', '3.10.9', False),
])

def test_satisfies(spec, version, want):
    assert sc.satisfies(version, spec) is want

@pytest.mark.parametrize('lower, higher', [
    ('3.11.0-0', '3.11.0'),        # a release outranks its prereleases
    ('3.11.0-1', '3.11.0-2'),      # numeric identifiers compare numerically
    ('3.11.0-1', '3.11.0-beta'),   # numeric ranks below alphanumeric
    ('3.10.9', '3.11.0'),
])
def test_version_ordering(lower, higher):
    assert sc.parse_version(lower) < sc.parse_version(higher)


def test_build_metadata_is_ignored():
    assert sc.parse_version('3.13.1+hotfix') == sc.parse_version('3.13.1')


@pytest.mark.parametrize('spec', [
    '>=3.0.0 <4.0.0 || >=5.0.0',   # alternatives
    '>=3.0.0, <4.0.0',             # comma ranges
    'not a constraint',
    None,
])
def test_unparseable_declines_rather_than_excluding(spec):
    assert sc.satisfies('3.13.1', spec) is None
    assert sc.excluded_reason({'environment': {'sdk': spec}}, '3.47.1', '3.13.1') is None


def test_excluded_reason_names_the_offender():
    r = sc.excluded_reason({'environment': {'sdk': '>=3.20.0 <4.0.0'}},
                           '3.47.1', '3.13.1')
    assert r is not None
    assert 'Dart' in r and '3.13.1' in r and '>=3.20.0' in r


def test_flutter_constraint_is_checked_too():
    r = sc.excluded_reason({'environment': {'flutter': '>=4.0.0'}},
                           '3.47.1', '3.13.1')
    assert r is not None and 'Flutter' in r


def test_compatible_app_is_not_excluded():
    # the shape the appstream_dart example app declares
    assert sc.excluded_reason({'environment': {'sdk': '^3.10.1'}},
                              '3.47.1', '3.13.1') is None


@pytest.mark.parametrize('obj', [
    {}, {'environment': None}, {'environment': 'nonsense'}, None,
])
def test_a_pubspec_without_a_usable_environment_is_not_excluded(obj):
    assert sc.excluded_reason(obj, '3.47.1', '3.13.1') is None


def test_unknown_pinned_version_disables_the_gate():
    # pinned_versions() returns None when FLUTTER_SDK_TAG is AUTOINC or the
    # release feed has no dart_sdk_version. Nothing should be excluded then.
    assert sc.excluded_reason({'environment': {'sdk': '>=99.0.0'}},
                              None, None) is None


def test_pinned_versions_reads_the_layer():
    flutter, dart = sc.pinned_versions()
    assert flutter and dart
    assert sc.parse_version(flutter) and sc.parse_version(dart)


# --- path dependencies (#850 case 2) -----------------------------------------

def _repo(tmp_path, app_env, dep_env, dep_rel='../pkg'):
    import yaml
    app = tmp_path / 'app'
    pkg = tmp_path / 'pkg'
    app.mkdir(); pkg.mkdir()
    (app / 'pubspec.yaml').write_text(yaml.safe_dump({
        'name': 'app', 'environment': {'sdk': app_env},
        'dependencies': {'pkg': {'path': dep_rel}}}))
    (pkg / 'pubspec.yaml').write_text(yaml.safe_dump({
        'name': 'pkg', 'environment': {'sdk': dep_env}}))
    return app


def _read(p):
    import yaml
    return yaml.safe_load(open(p))


def _reason(app):
    return sc.path_dependency_reason(str(app), _read(app / 'pubspec.yaml'),
                                     '3.47.1', '3.13.1', _read)


def test_an_incompatible_path_dependency_excludes_the_app(tmp_path):
    # the app itself is fine; what it depends on is not, and only a resolve
    # would otherwise find that
    app = _repo(tmp_path, '^3.11.0-0', '>=2.17.0 <3.0.0')
    r = _reason(app)
    assert r is not None
    assert 'depends on pkg by path' in r and 'Dart' in r


def test_a_compatible_path_dependency_is_not_an_exclusion(tmp_path):
    assert _reason(_repo(tmp_path, '^3.11.0-0', '^3.11.0-0')) is None


def test_it_follows_path_dependencies_transitively(tmp_path):
    import yaml
    app = _repo(tmp_path, '^3.11.0-0', '^3.11.0-0')
    deep = tmp_path / 'deep'
    deep.mkdir()
    (deep / 'pubspec.yaml').write_text(yaml.safe_dump({
        'name': 'deep', 'environment': {'sdk': '>=2.16.0 <3.0.0'}}))
    (tmp_path / 'pkg' / 'pubspec.yaml').write_text(yaml.safe_dump({
        'name': 'pkg', 'environment': {'sdk': '^3.11.0-0'},
        'dependencies': {'deep': {'path': '../deep'}}}))
    r = _reason(app)
    assert r is not None and 'deep' in r


def test_mutually_referential_paths_do_not_recurse_forever(tmp_path):
    import yaml
    app = _repo(tmp_path, '^3.11.0-0', '^3.11.0-0')
    (tmp_path / 'pkg' / 'pubspec.yaml').write_text(yaml.safe_dump({
        'name': 'pkg', 'environment': {'sdk': '^3.11.0-0'},
        'dependencies': {'app': {'path': '../app'}}}))
    assert _reason(app) is None


def test_a_path_that_does_not_exist_is_not_our_failure(tmp_path):
    # pub will complain about it; guessing here would report the wrong problem
    app = _repo(tmp_path, '^3.11.0-0', '^3.11.0-0', dep_rel='../nowhere')
    assert _reason(app) is None


def test_hosted_dependencies_are_not_followed(tmp_path):
    import yaml
    app = tmp_path / 'app'
    app.mkdir()
    (app / 'pubspec.yaml').write_text(yaml.safe_dump({
        'name': 'app', 'environment': {'sdk': '^3.11.0-0'},
        'dependencies': {'http': '^1.0.0', 'flutter': {'sdk': 'flutter'}}}))
    assert _reason(app) is None
