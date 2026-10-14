# Shader cache on a read-only image

The engine's persistent cache is a runtime write. Where it goes, and what happens
when that place is not writable, is the embedder's choice, and the three embedders
this layer ships do not agree. This is what each does at the revisions pinned
here, and what an image is expected to provide.

## What each embedder does

| embedder | cache path | read-only rootfs |
|---|---|---|
| ivi-homescreen, flutter-auto | `$XDG_CONFIG_HOME/<dir>/<app>`, else `$HOME` | resolved without throwing, three outcomes: writable, read-only, unusable |
| flutter-pi | the asset bundle, under `${datadir}/flutter` | one `Could not write cache contents to persistent store.` per store; no caching, no crash |
| flutter-elinux | never set | no cache at all, written or read |

ivi-homescreen is the only one that tells the engine what it found
(`shell/engine.cc`):

    m_args.persistent_cache_path =
        m_cache_dir.usable() ? m_cache_dir.path.c_str() : nullptr;
    m_args.is_persistent_cache_read_only =
        m_cache_dir.state == ihs::CacheDirState::kReadOnly;

That matters for the second deployment below. It arrived with
toyota-connected/ivi-homescreen#681; before that a read-only rootfs ended the
process in `std::terminate`, because the directory was created from a
constructor's initializer list with the throwing overload.

flutter-pi sets `is_persistent_cache_read_only = false` explicitly, so a cache
shipped in its bundle is not read even though the bundle is exactly where one
would land on a read-only image.

## What an image should provide

Pick one. Neither needs anything from this layer.

**A writable cache directory.** Mount something writable over the directory the
embedder resolves -- `$XDG_CONFIG_HOME` if the launcher sets it, else `$HOME`.
The cache then warms on first run and survives reboots if the mount does. This is
the ordinary case and the one to choose unless the image is strictly read-only.

**A cache warmed in advance, shipped read-only.** ivi-homescreen and flutter-auto
hand the engine `is_persistent_cache_read_only` when the directory is not
writable, so a cache already there is read and nothing is written. First frames
come up without recompiling shaders, on an image with nothing writable at all.

The layer ships no tooling to produce that cache, and it cannot: the engine writes
it while the application runs. Capture it from a device or an emulated run, then
install it in a bbappend at the path the embedder resolves. It is tied to the
engine revision and the GPU driver, so it has to be regenerated with either.

## Where the path comes from

Nothing in this layer sets `HOME` or `XDG_CONFIG_HOME` at runtime, and it ships no
systemd units, so the path is whatever starts the embedder. With poky's
`ROOT_HOME = "/home/root"` and no writable mount over it, a read-only rootfs
means the unusable case above: on ivi-homescreen a log line and no caching, on
flutter-pi a warning per store.

See [docs/environment-variables.md](environment-variables.md) for the rest of what
the tooling reads.

## Impeller does not participate

Impeller Vulkan has its own driver pipeline cache, written as
`flutter.impeller.vkcache`, but it needs `ContextVK::Settings::cache_directory`,
and the only place the engine assigns that is the Android embedder. The embedder
API never wires it, so under any embedder here that file is neither written nor
read, in any runtime mode, on any filesystem. First-run pipeline compilation is
not cacheable through this path at all. That is an upstream gap rather than
something an image can arrange.
