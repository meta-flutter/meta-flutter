# Environment variables

The Flutter SDK, `flutter_tools` and `pub` read a good deal of their behavior
out of the environment. Our recipes run them, so the environment a task hands
them is part of the build definition, not incidental. This file records what
reads what, and which of it we pin.

Verified against Flutter 3.47.5 and the `pub` bundled with Dart 3.13.4. Line
references are to that release.

## What reaches a bitbake task at all

Bitbake does not pass the invoking shell's environment through. It keeps what
`BB_ENV_PASSTHROUGH_ADDITIONS` names, which `openembedded-core`'s
`scripts/oe-buildenv-internal` sets to:

    MACHINE DISTRO TCMODE TCLIBC
    HTTP_PROXY http_proxy HTTPS_PROXY https_proxy FTP_PROXY ftp_proxy
    FTPS_PROXY ftps_proxy ALL_PROXY all_proxy NO_PROXY no_proxy
    SSH_AGENT_PID SSH_AUTH_SOCK BB_SRCREV_POLICY SDKMACHINE
    BB_NUMBER_THREADS BB_NO_NETWORK PARALLEL_MAKE GIT_PROXY_COMMAND
    SOCKS5_PASSWD SOCKS5_USER SCREENDIR STAMPS_DIR BBPATH_EXTRA
    BB_SETSCENE_ENFORCE BB_LOGCONFIG

None of the Flutter or pub variables below are on that list, so a developer's
`PUB_HOSTED_URL` or `FLUTTER_STORAGE_BASE_URL` cannot leak in from their shell.
What can reach a task is what `local.conf` exports, what the build container
sets, and what a CI workflow step sets.

## What our recipes set

`recipes-graphics/flutter-sdk/flutter-sdk_git.bb` starts from the task
environment (`env = os.environ`) and sets:

| variable | value | why |
|---|---|---|
| `PATH` | `${S}/bin` prepended | so `flutter` and `dart` resolve to the SDK being built, not one on the host |
| `PUB_CACHE` | `${S}/.pub-cache` | the cache staged from SRC_URI, which the resolve must use |
| `HOME` | `${WORKDIR}` | dart needs a writable home -- dart-lang/sdk#41560 |
| `XDG_CONFIG_HOME` | `${WORKDIR}` | flutter needs one -- flutter/flutter#59430 |
| `CURL_CA_BUNDLE` | native sysroot bundle | so curl trusts the same CAs the build does |
| `http_proxy`, `https_proxy`, `HTTP_PROXY`, `HTTPS_PROXY` | passed through when set | proxied builds |
| `NO_PROXY` | `localhost,127.0.0.1,::1` | keep loopback direct |

`conf/include/common.inc` builds the same shape for app recipes, and
`classes/pub-cache.bbclass` points `PUB_CACHE` at the vendored cache.

## Read by flutter_tools

| variable | effect | we set it |
|---|---|---|
| `FLUTTER_ROOT` | the SDK root; the `flutter` shell entry exports it and the tool reads it throughout | no -- the entry script handles it |
| `FLUTTER_STORAGE_BASE_URL` | mirror base for artifact downloads (`kFlutterStorageBaseUrl`) | no |
| `PUB_HOSTED_URL` | pub.dev override (`kPubDevOverride`) | no -- see the warning below |
| `PUB_CACHE` | package cache location | yes |
| `PUB_ENVIRONMENT` | appended to pub's reported environment; telemetry context only | no |
| `PUB_SUMMARY_ONLY` | the tool sets `'1'` itself to quieten pub's output | tool-set |
| `PUB_MAX_HTTP_RETRIES` | pub's retry count; timing only | no |
| `FLUTTER_SUPPRESS_ANALYTICS` | `'true'` suppresses analytics | no -- we pass `--suppress-analytics` instead |
| `FLUTTER_ALREADY_LOCKED` | `'true'` means the caller holds the SDK lock, so the tool does not take it again | no |
| `FLUTTER_GIT_URL` | overrides the upstream git URL used for version checks | no |
| `FLUTTER_HOST` | reported as the client IDE in telemetry | no |
| `FLUTTER_TEST` | marks a test run | no |
| `SERVER_PORT` | dev/test server port | no |
| `BOT`, `TRAVIS`, `LUCI_CI`, `CONTINUOUS_INTEGRATION`, `CHROME_HEADLESS` | CI detection; changes analytics and terminal animations | no |
| `TERM` | terminal capabilities, so output formatting | no |

Windows and Apple-only variables (`LOCALAPPDATA`, `USERPROFILE`, `HOMEDRIVE`,
`HOMEPATH`, `PROGRAMFILES`, `IOS_SIMULATOR_LOG_FILE_PATH`, `ANDROID_AVD_HOME`)
are read by the same code but never apply here.

## Set by flutter_tools, not read from us

`FLUTTER_ENGINE`, `FLUTTER_ENGINE_SWITCHES`, `LOCAL_ENGINE` and
`LOCAL_ENGINE_HOST` appear in `flutter_tools` only as assignments into a child
process's environment, for the local-engine workflow. That workflow is selected
by command-line flags. Setting them in a task environment is not a supported way
to redirect the engine.

## PUB_HOSTED_URL changes the cache layout, not just the source

pub stores hosted packages under `hosted/<host-directory>/<name>-<version>`,
where the host directory is derived from the hosted URL. Point `PUB_HOSTED_URL`
somewhere else and pub reads and writes a *different* directory: a cache
populated as `hosted/pub.dev/...` becomes invisible, and an offline resolve
fails with "could not find package ... in cache" that looks like a missing
package rather than a redirected cache.

This is easy to hit when testing offline behavior by pointing the URL at a dead
port. Use network isolation instead -- `unshare -rn` -- so the cache layout stays
the one the build actually uses.

## What is path dependent in the staged SDK

Two builds of `flutter-sdk-native` differing only in `TMPDIR` produce 36265
identical files out of 36294. The 29 that differ are all path or time dependent
rather than nondeterministic:

| what | files | why |
|---|---|---|
| relocated native binaries | 14 | sstate rewrites absolute sysroot paths into ELF binaries (`dart`, `dartvm`, `gen_snapshot`, `impellerc`, `flutter_tester`, `font-subset`, `dartaotruntime*`, `wasm-opt`) |
| `package_config.json` | 2 | every `rootUri` is an absolute path into the work directory |
| pub `active_roots` | 4 | the file *names* are hashes of the workspace root path |
| pub git caches | 8 | `.git/config`, `index` and reflogs from the clone |
| `flutter_version_check.stamp` | 1 | a timestamp |

Worth knowing before treating any of it as a reproducibility defect. The
packaged target output is a separate question and is byte-reproducible -- see
issue #271.
