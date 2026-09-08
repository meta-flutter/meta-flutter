# SPDX-License-Identifier: MIT
"""requirements.txt has to describe what the code actually imports.

Hand-maintained dependency lists drift, and this one had: the README named
pycurl and pyyaml while get_engine_commit() also needs certifi, and CI
installed a third set again. Nothing noticed, because pycurl and certifi are
imported inside functions -- importing a module does not fail without them, so
the tests passed and a fresh virtualenv did not. See #889.
"""
import ast
import pathlib
import sys

TOOLS = pathlib.Path(__file__).resolve().parent.parent

# import name -> distribution name, where they differ
DISTRIBUTION = {'yaml': 'pyyaml'}


def _declared():
    lines = (TOOLS / 'requirements.txt').read_text().splitlines()
    return {l.strip().lower() for l in lines
            if l.strip() and not l.startswith('#')}


def _imported():
    stdlib = set(sys.stdlib_module_names)
    local = {p.stem for p in TOOLS.rglob('*.py')}
    found = set()
    for path in TOOLS.rglob('*.py'):
        if 'tests' in path.parts:
            continue
        tree = ast.parse(path.read_text(), str(path))
        for node in ast.walk(tree):
            if isinstance(node, ast.Import):
                for alias in node.names:
                    found.add(alias.name.split('.')[0])
            elif isinstance(node, ast.ImportFrom) and node.module and not node.level:
                found.add(node.module.split('.')[0])
    return {DISTRIBUTION.get(m, m) for m in found - stdlib - local}


def test_every_third_party_import_is_declared():
    missing = _imported() - _declared()
    assert not missing, (
        f'imported by tools/ but absent from requirements.txt: {sorted(missing)}')


def test_nothing_declared_is_unused():
    # a dependency nobody imports is one someone installs for no reason, and a
    # hint that the code it supported has gone
    unused = _declared() - _imported()
    assert not unused, (
        f'in requirements.txt but imported nowhere: {sorted(unused)}')


def test_the_scan_actually_finds_things():
    # a scan that silently returns nothing would make both tests above vacuous
    found = _imported()
    assert found, 'the import scan found nothing, so it is not checking anything'
    assert 'pycurl' in found, 'pycurl is imported inside functions and must still be found'
