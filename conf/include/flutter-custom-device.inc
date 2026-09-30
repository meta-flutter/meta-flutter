#
# Copyright (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# Ships a Flutter custom-device config for the embedder a recipe builds, so a
# developer holding the matching Yocto SDK can "flutter run -d <id>" straight at
# the board. See meta-flutter#522.
#
# The generated file is one upstream custom_devices.json device object per file,
# installed as:
#
#   ${datadir}/flutter/custom-devices/${FLUTTER_CUSTOM_DEVICE_ID}.json
#
# It lands in ${PN}-dev, which is what populate_sdk pulls into the SDK target
# sysroot. flutter-custom-devices (nativesdk) merges it into the developer's
# custom_devices.json.
#
# Every knob below is ?=, so a bbappend overrides what it needs and leaves the
# rest alone:
#
#   FLUTTER_CUSTOM_DEVICE_HOST = "demo@board-42"
#   FLUTTER_CUSTOM_DEVICE_RUN_DEBUG = "@SSH@ @HOST@ /usr/bin/my-shell -b /tmp/@appName@"
#
# Three token forms are recognized in the command variables:
#
#   @localPath@ @appName@ @remotePath@ @devicePort@ @hostPort@
#       flutter_tools' own placeholders, substituted when the callback runs.
#       They are spelled with @ here because bitbake would expand the ${...}
#       form itself the moment any layer defined a variable of that name.
#
#   @HOST@ @HOSTNAME@
#       The board, filled in by flutter-custom-devices at merge time from
#       --host. A board address is not known at build time and is not worth a
#       rebuild, so by default it stays a token. Set the variables to literals
#       instead when the product does pin an address.
#
#   @SDK_DIR@ @SDK_SYSROOT@
#       The SDK install directory and its target sysroot, also filled in at
#       merge time. A product that supplies its own callback scripts ships them
#       in the SDK and references them this way -- never as an absolute path on
#       one developer's machine.
#
# The callbacks default to plain ssh/scp because that is the least opinionated
# thing that works. Nothing here assumes how a product installs or launches a
# bundle; override the command variables and the defaults go away.
#
# A bbappend that puts machine-specific values in any of these should also set
# PACKAGE_ARCH = "${MACHINE_ARCH}". The defaults deliberately are not.
#

FLUTTER_CUSTOM_DEVICE_ID ?= "${PN}"
FLUTTER_CUSTOM_DEVICE_LABEL ?= "${PN} (${DISTRO})"
FLUTTER_CUSTOM_DEVICE_SDK_NAME ?= "${DISTRO_NAME} ${DISTRO_VERSION}"
FLUTTER_CUSTOM_DEVICE_ENABLED ?= "1"

# Empty unless the target is one of the two architectures flutter_tools accepts
# for a custom device; anything else must stay null or the device is rejected
# at load.
FLUTTER_CUSTOM_DEVICE_PLATFORM ?= "${@fcd_platform(d)}"

# Who the default callbacks talk to, and how.
FLUTTER_CUSTOM_DEVICE_HOSTNAME ?= "@HOSTNAME@"
FLUTTER_CUSTOM_DEVICE_HOST ?= "@HOST@"
FLUTTER_CUSTOM_DEVICE_SSH_OPTS ?= "-o BatchMode=yes -o StrictHostKeyChecking=accept-new"
FLUTTER_CUSTOM_DEVICE_SSH ?= "ssh ${FLUTTER_CUSTOM_DEVICE_SSH_OPTS}"
FLUTTER_CUSTOM_DEVICE_SCP ?= "scp -r ${FLUTTER_CUSTOM_DEVICE_SSH_OPTS}"

# Where the bundle lands on the target. flutter run pushes a debug bundle, so
# this is a scratch location, not an installed package path.
FLUTTER_CUSTOM_DEVICE_APP_DIR ?= "/tmp/@appName@"

# The one genuinely embedder-specific line: how to launch a bundle directory.
# A recipe that requires this file is expected to set it.
FLUTTER_CUSTOM_DEVICE_EXEC ?= ""

FLUTTER_CUSTOM_DEVICE_PING ?= "ping -w 1 -c 1 ${FLUTTER_CUSTOM_DEVICE_HOSTNAME}"
FLUTTER_CUSTOM_DEVICE_PING_SUCCESS_REGEX ?= "[<=]\d+ms"
FLUTTER_CUSTOM_DEVICE_POST_BUILD ?= ""
FLUTTER_CUSTOM_DEVICE_INSTALL ?= "${FLUTTER_CUSTOM_DEVICE_SCP} @localPath@ ${FLUTTER_CUSTOM_DEVICE_HOST}:${FLUTTER_CUSTOM_DEVICE_APP_DIR}"
FLUTTER_CUSTOM_DEVICE_UNINSTALL ?= "${FLUTTER_CUSTOM_DEVICE_SSH} ${FLUTTER_CUSTOM_DEVICE_HOST} rm -rf ${FLUTTER_CUSTOM_DEVICE_APP_DIR}"
FLUTTER_CUSTOM_DEVICE_RUN_DEBUG ?= "${FLUTTER_CUSTOM_DEVICE_SSH} ${FLUTTER_CUSTOM_DEVICE_HOST} ${FLUTTER_CUSTOM_DEVICE_EXEC}"
FLUTTER_CUSTOM_DEVICE_FORWARD_PORT ?= "${FLUTTER_CUSTOM_DEVICE_SSH} -o ExitOnForwardFailure=yes -L 127.0.0.1:@hostPort@:127.0.0.1:@devicePort@ ${FLUTTER_CUSTOM_DEVICE_HOST} 'echo Port forwarding success; read'"
FLUTTER_CUSTOM_DEVICE_FORWARD_PORT_SUCCESS_REGEX ?= "Port forwarding success"
FLUTTER_CUSTOM_DEVICE_SCREENSHOT ?= ""
FLUTTER_CUSTOM_DEVICE_READ_LOGS ?= ""

FLUTTER_CUSTOM_DEVICE_DIR = "${datadir}/flutter/custom-devices"

FILES_${PN}-dev += "${FLUTTER_CUSTOM_DEVICE_DIR}"

# flutter_tools' own placeholders. Spelled @name@ in the variables above and
# rewritten to the ${name} form on the way into the JSON.
FLUTTER_CUSTOM_DEVICE_PLACEHOLDERS = "localPath appName remotePath devicePort hostPort"

# Expanded by the merge script: not by bitbake, not by flutter_tools.
FLUTTER_CUSTOM_DEVICE_SDK_TOKENS = "HOST HOSTNAME SDK_DIR SDK_SYSROOT"


def fcd_platform(d):
    """flutter_tools accepts linux-arm64 and linux-x64 and nothing else."""
    return {'aarch64': 'linux-arm64', 'x86_64': 'linux-x64'}.get(d.getVar('TARGET_ARCH'), '')


def fcd_cmd(d, var):
    """A command variable as a JSON array, or None when it is empty."""
    import re
    import shlex

    value = d.getVar(var)
    if not value or not value.strip():
        return None
    names = (d.getVar('FLUTTER_CUSTOM_DEVICE_PLACEHOLDERS') or '').split()
    token = re.compile('@(' + '|'.join(names) + ')@')
    return [token.sub('${' + chr(92) + '1}', word) for word in shlex.split(value)]


python flutter_custom_device_write() {
    import json
    import os
    import re

    # A layer that defines one of the placeholder names would have eaten the
    # ${...} form during expansion, silently, leaving a config that looks right
    # and points somewhere else. The @name@ spelling is immune, so all that is
    # left to catch is the recipe that used the literal form anyway.
    for name in (d.getVar('FLUTTER_CUSTOM_DEVICE_PLACEHOLDERS') or '').split():
        if d.getVar(name) is not None:
            bb.fatal("flutter-custom-device: the datastore defines '%s', so the "
                     "literal placeholder form is unsafe here. Write @%s@ in the "
                     "FLUTTER_CUSTOM_DEVICE_* variables." % (name, name))

    device_id = d.getVar('FLUTTER_CUSTOM_DEVICE_ID')
    if not device_id:
        bb.fatal("flutter-custom-device: FLUTTER_CUSTOM_DEVICE_ID is empty")

    forward_port = fcd_cmd(d, 'FLUTTER_CUSTOM_DEVICE_FORWARD_PORT')
    forward_regex = d.getVar('FLUTTER_CUSTOM_DEVICE_FORWARD_PORT_SUCCESS_REGEX')
    if forward_port and not forward_regex:
        bb.fatal("flutter-custom-device: FLUTTER_CUSTOM_DEVICE_FORWARD_PORT needs "
                 "FLUTTER_CUSTOM_DEVICE_FORWARD_PORT_SUCCESS_REGEX; flutter_tools "
                 "rejects the config otherwise")

    config = {
        'id': device_id,
        'label': d.getVar('FLUTTER_CUSTOM_DEVICE_LABEL'),
        'sdkNameAndVersion': d.getVar('FLUTTER_CUSTOM_DEVICE_SDK_NAME'),
        'platform': d.getVar('FLUTTER_CUSTOM_DEVICE_PLATFORM') or None,
        'enabled': bb.utils.to_boolean(d.getVar('FLUTTER_CUSTOM_DEVICE_ENABLED'), True),
        'ping': fcd_cmd(d, 'FLUTTER_CUSTOM_DEVICE_PING'),
        'pingSuccessRegex': d.getVar('FLUTTER_CUSTOM_DEVICE_PING_SUCCESS_REGEX') or None,
        'postBuild': fcd_cmd(d, 'FLUTTER_CUSTOM_DEVICE_POST_BUILD'),
        'install': fcd_cmd(d, 'FLUTTER_CUSTOM_DEVICE_INSTALL'),
        'uninstall': fcd_cmd(d, 'FLUTTER_CUSTOM_DEVICE_UNINSTALL'),
        'runDebug': fcd_cmd(d, 'FLUTTER_CUSTOM_DEVICE_RUN_DEBUG'),
        'forwardPort': forward_port,
        'forwardPortSuccessRegex': forward_regex if forward_port else None,
        'screenshot': fcd_cmd(d, 'FLUTTER_CUSTOM_DEVICE_SCREENSHOT'),
        'readLogs': fcd_cmd(d, 'FLUTTER_CUSTOM_DEVICE_READ_LOGS'),
    }

    # The fields flutter_tools refuses to revive as null.
    for key in ('id', 'label', 'sdkNameAndVersion', 'ping', 'install',
                'uninstall', 'runDebug'):
        if not config[key]:
            bb.fatal("flutter-custom-device: '%s' is empty; flutter_tools requires it" % key)

    # With no launcher named, the default runDebug reduces to "ssh <opts>
    # <host>": it connects, runs the login shell and exits. That is a device
    # that looks like it works and never starts the app, so refuse it here.
    if not d.getVar('FLUTTER_CUSTOM_DEVICE_EXEC'):
        import shlex
        bare = shlex.split('%s %s' % (d.getVar('FLUTTER_CUSTOM_DEVICE_SSH'),
                                      d.getVar('FLUTTER_CUSTOM_DEVICE_HOST')))
        if config['runDebug'] == bare:
            bb.fatal("flutter-custom-device: set FLUTTER_CUSTOM_DEVICE_EXEC to the "
                     "embedder command that launches a bundle directory, or replace "
                     "FLUTTER_CUSTOM_DEVICE_RUN_DEBUG outright")

    text = json.dumps(config, indent=2) + '\n'

    # Typo guard: every @token@ is either one of ours or a mistake that would
    # otherwise ship verbatim into the developer's config.
    allowed = set((d.getVar('FLUTTER_CUSTOM_DEVICE_SDK_TOKENS') or '').split())
    stray = set(re.findall('@([A-Za-z_][A-Za-z0-9_]*)@', text)) - allowed
    if stray:
        bb.fatal("flutter-custom-device: unknown token(s) %s in the generated "
                 "config; known tokens are %s plus the @placeholder@ forms"
                 % (', '.join(sorted(stray)), ', '.join(sorted(allowed))))

    destdir = d.getVar('D') + d.getVar('FLUTTER_CUSTOM_DEVICE_DIR')
    bb.utils.mkdirhier(destdir)
    with open(os.path.join(destdir, device_id + '.json'), 'w') as handle:
        handle.write(text)
}

# fcd_cmd() reaches the variables through a helper, so the code parser cannot
# see the getVar names. Spell them out or a changed callback does not rebuild.
flutter_custom_device_write[vardeps] += "\
    FLUTTER_CUSTOM_DEVICE_ID \
    FLUTTER_CUSTOM_DEVICE_LABEL \
    FLUTTER_CUSTOM_DEVICE_SDK_NAME \
    FLUTTER_CUSTOM_DEVICE_PLATFORM \
    FLUTTER_CUSTOM_DEVICE_ENABLED \
    FLUTTER_CUSTOM_DEVICE_EXEC \
    FLUTTER_CUSTOM_DEVICE_SSH \
    FLUTTER_CUSTOM_DEVICE_HOST \
    FLUTTER_CUSTOM_DEVICE_PING \
    FLUTTER_CUSTOM_DEVICE_PING_SUCCESS_REGEX \
    FLUTTER_CUSTOM_DEVICE_POST_BUILD \
    FLUTTER_CUSTOM_DEVICE_INSTALL \
    FLUTTER_CUSTOM_DEVICE_UNINSTALL \
    FLUTTER_CUSTOM_DEVICE_RUN_DEBUG \
    FLUTTER_CUSTOM_DEVICE_FORWARD_PORT \
    FLUTTER_CUSTOM_DEVICE_FORWARD_PORT_SUCCESS_REGEX \
    FLUTTER_CUSTOM_DEVICE_SCREENSHOT \
    FLUTTER_CUSTOM_DEVICE_READ_LOGS \
    "

do_install[postfuncs] += "flutter_custom_device_write"
