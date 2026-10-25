# Security Policy

## Reporting a vulnerability

Please do not open a public issue for an unreleased or urgent vulnerability.

Send it to the maintainer at joel.winarske@linux.com with as much detail as
you have: the affected recipe and its version, the branch, and a reproducer
if there is one.

For a problem that is already public, one with an assigned CVE for example,
open a normal GitHub issue instead.

This is a community-maintained Yocto layer, not a product with a security
team. Reports are handled by the maintainer on a best-effort basis, with no
guaranteed response time.

## Branches that receive fixes

Fixes land on wrynose first and are ported from there:

    wrynose     oe-core wrynose
    master      oe-core master
    scarthgap   oe-core scarthgap
    kirkstone   oe-core kirkstone
    dunfell     oe-core dunfell

honister is not maintained and receives no fixes.

## Where a vulnerability belongs

This layer builds the Flutter engine, the Dart SDK and applications from
upstream sources. A flaw in Flutter, Dart, or the engine's vendored
dependencies belongs upstream with those projects. Report it here when the
problem is in this layer: a recipe, a patch carried here, a class, or a
version this layer pins.
