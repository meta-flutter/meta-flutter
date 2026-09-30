# Flutter custom devices

`flutter run -d <device>` can drive a board over ssh through a *custom device*:
a set of callbacks -- ping, install, run, forward port -- that flutter_tools
shells out to. Upstream expects the developer to hand-write them into
`custom_devices.json`. This layer generates them from the recipe that builds the
embedder instead, ships them in the SDK, and merges them into the developer's
config from there.

See [meta-flutter#522](https://github.com/meta-flutter/meta-flutter/issues/522).

## Building it

The config rides along with the embedder. `ivi-homescreen` and `flutter-auto`
already generate one; any other recipe gets one with

    require conf/include/flutter-custom-device.inc

The file lands in `${PN}-dev` as

    ${datadir}/flutter/custom-devices/${FLUTTER_CUSTOM_DEVICE_ID}.json

which is what `populate_sdk` pulls into the SDK target sysroot. Add the host
side to the SDK in `local.conf`:

    TOOLCHAIN_HOST_TASK:append = " nativesdk-flutter-custom-devices"

then

    bitbake <image> -c populate_sdk

## Using it

From an installed SDK:

    $ . <sdk>/environment-setup-aarch64-poky-linux
    $ flutter-custom-devices list
      flutter-auto      flutter-auto (poky)    [needs HOSTNAME, HOST]
      ivi-homescreen    ivi-homescreen (poky)  [needs HOSTNAME, HOST]

    $ flutter config --enable-custom-devices
    $ flutter-custom-devices merge --all --host root@192.168.1.50
    added    flutter-auto
    added    ivi-homescreen
    wrote    /home/you/.config/flutter/custom_devices.json

    $ flutter devices
    $ flutter run -d ivi-homescreen

`merge` replaces an entry whose `id` matches and leaves every other entry in the
file alone, so an SDK merge never eats a hand-written device. The previous file
is kept as `custom_devices.json.bak`. `-n` prints the merged result instead of
writing it, and `remove <id>` drops entries again.

The config path is the one flutter_tools itself resolves:
`~/.flutter_custom_devices.json` if it exists, else `$XDG_CONFIG_HOME`
(verbatim -- upstream does not append `flutter` to it), else
`~/.config/flutter/custom_devices.json`. `--config` overrides it.

## Tokens

A board address is not known at build time and is not worth a rebuild, so the
generated config carries tokens that are filled in at merge time:

| Token           | Filled from                                            |
|-----------------|--------------------------------------------------------|
| `@HOST@`        | `--host` (user defaults to `root`), `$FLUTTER_TARGET_HOST` |
| `@HOSTNAME@`    | the address part of the same                           |
| `@SDK_DIR@`     | the SDK install directory, or `--sdk-dir`              |
| `@SDK_SYSROOT@` | the SDK target sysroot, or `--target-sysroot`          |

A config that still has an unfilled token is refused with the flag that supplies
it, rather than merged and left to fail as a confusing ssh error later.

flutter_tools' own placeholders -- `${localPath}` `${appName}` `${remotePath}`
`${devicePort}` `${hostPort}` -- are spelled `@localPath@` and so on in the
recipe variables, because bitbake would expand the `${...}` form itself the
moment any layer defined a variable of that name. The include rewrites them on
the way into the JSON and fails the build on an `@token@` it does not know.

## Overriding in a bbappend

Every variable is `?=`. Point it at a real board and give it real callbacks:

    FLUTTER_CUSTOM_DEVICE_ID = "board-42"
    FLUTTER_CUSTOM_DEVICE_HOST = "demo@board-42.lan"
    FLUTTER_CUSTOM_DEVICE_HOSTNAME = "board-42.lan"
    FLUTTER_CUSTOM_DEVICE_POST_BUILD = "@SDK_DIR@/tools/sign-bundle @localPath@"
    FLUTTER_CUSTOM_DEVICE_RUN_DEBUG = "@SDK_DIR@/tools/launch @HOST@ /tmp/@appName@"
    PACKAGE_ARCH = "${MACHINE_ARCH}"

`PACKAGE_ARCH` matters as soon as a value is machine-specific: the defaults
deliberately are not, so the include does not set it.

The callbacks default to plain ssh and scp because that is the least opinionated
thing that works. Nothing in the include assumes how a product installs or
launches a bundle -- override the command variables and the defaults go away.
What the include does insist on is that a product's own callbacks are reachable
from the SDK, through `@SDK_DIR@`, rather than by an absolute path that happens
to exist on one developer's machine.

| Variable                                            | Default |
|-----------------------------------------------------|---------|
| `FLUTTER_CUSTOM_DEVICE_ID`                          | `${PN}` |
| `FLUTTER_CUSTOM_DEVICE_LABEL`                       | `${PN} (${DISTRO})` |
| `FLUTTER_CUSTOM_DEVICE_SDK_NAME`                    | `${DISTRO_NAME} ${DISTRO_VERSION}` |
| `FLUTTER_CUSTOM_DEVICE_ENABLED`                     | `1` |
| `FLUTTER_CUSTOM_DEVICE_PLATFORM`                    | `linux-arm64` or `linux-x64`, else null |
| `FLUTTER_CUSTOM_DEVICE_HOST`                        | `@HOST@` |
| `FLUTTER_CUSTOM_DEVICE_HOSTNAME`                    | `@HOSTNAME@` |
| `FLUTTER_CUSTOM_DEVICE_SSH_OPTS`                    | `-o BatchMode=yes -o StrictHostKeyChecking=accept-new` |
| `FLUTTER_CUSTOM_DEVICE_SSH`                         | `ssh ${FLUTTER_CUSTOM_DEVICE_SSH_OPTS}` |
| `FLUTTER_CUSTOM_DEVICE_SCP`                         | `scp -r ${FLUTTER_CUSTOM_DEVICE_SSH_OPTS}` |
| `FLUTTER_CUSTOM_DEVICE_APP_DIR`                     | `/tmp/@appName@` |
| `FLUTTER_CUSTOM_DEVICE_EXEC`                        | empty; the embedder recipe sets it |
| `FLUTTER_CUSTOM_DEVICE_PING`                        | `ping -w 1 -c 1 <hostname>` |
| `FLUTTER_CUSTOM_DEVICE_PING_SUCCESS_REGEX`          | `[<=]\d+ms` |
| `FLUTTER_CUSTOM_DEVICE_POST_BUILD`                  | empty (null) |
| `FLUTTER_CUSTOM_DEVICE_INSTALL`                     | scp the bundle to `APP_DIR` |
| `FLUTTER_CUSTOM_DEVICE_UNINSTALL`                   | rm the bundle |
| `FLUTTER_CUSTOM_DEVICE_RUN_DEBUG`                   | ssh + `EXEC` |
| `FLUTTER_CUSTOM_DEVICE_FORWARD_PORT`                | ssh -L |
| `FLUTTER_CUSTOM_DEVICE_FORWARD_PORT_SUCCESS_REGEX`  | `Port forwarding success` |
| `FLUTTER_CUSTOM_DEVICE_SCREENSHOT`                  | empty (null) |
| `FLUTTER_CUSTOM_DEVICE_READ_LOGS`                   | empty (null) |

A command variable is a shell command line, split with `shlex` on the way into
the JSON array. Quote a remote command that has to stay one argument:
`'echo ready; read'`.

## Notes

* `flutter run` on a custom device pushes a *debug* bundle, so the image needs a
  debug or profile engine. A release-only image will not run it.
* `ping` has to be on the host, and the board has to answer it. A board behind a
  firewall that drops ICMP never shows up in `flutter devices`; replace
  `FLUTTER_CUSTOM_DEVICE_PING` with something that does work, such as
  `ssh <host> true`, and set the success regex to match.
* The install callback copies the bundle over ssh on every run. That is fine on
  a LAN and painful over anything slower; `postBuild` is the hook for a product
  that wants to ship a delta instead.
* `nativesdk-flutter-custom-devices` deliberately does not depend on
  `nativesdk-python3`. It is one stdlib-only script, and every machine that can
  use a Yocto SDK already has a host `python3`.
