SUMMARY = "wayland_sink_with_video_overlay"
DESCRIPTION = "Rust Wayland example of a video overlay using gstreamer WaylandSink"
AUTHOR = "Joel Winarske <joel.winarske@linux.com>"
HOMEPAGE = "https://github.com/jwinarske/waylandsink-with-video-overlay-rs"
BUGTRACKER = "https://github.com/jwinarske/waylandsink-with-video-overlay-rs/issues"
SECTION = "graphics"
CVE_PRODUCT = "wayland_sink_with_video_overlay"

LICENSE = "MIT | Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE-MIT;md5=6a58c3c6b9fb9286bf967dc096f52ba3 \
                    file://LICENSE-APACHE;md5=1836efb2eb779966696f473ee8540542 \
                   "

REQUIRED_DISTRO_FEATURES = "wayland"

DEPENDS += "gstreamer1.0-libav \
            wayland \
           "

SRC_URI = "git://git@github.com/jwinarske/waylandsink-with-video-overlay-rs.git;protocol=ssh;nobranch=1;branch=main"
SRCREV = "11f5ae7533be0b2d0b046521ecc2113e0a2a30d4"
S = "${WORKDIR}/git"

inherit cargo features_check

CARGO_SRC_DIR = ""

SRC_URI_append = " \
    crate://crates.io/anyhow/1.0.38 \
    crate://crates.io/autocfg/1.0.1 \
    crate://crates.io/bitflags/1.2.1 \
    crate://crates.io/cc/1.0.66 \
    crate://crates.io/cfg-if/0.1.10 \
    crate://crates.io/cfg-if/1.0.0 \
    crate://crates.io/derive_more/0.99.11 \
    crate://crates.io/dlib/0.4.2 \
    crate://crates.io/downcast-rs/1.2.0 \
    crate://crates.io/either/1.6.1 \
    crate://crates.io/futures-channel/0.3.12 \
    crate://crates.io/futures-core/0.3.12 \
    crate://crates.io/futures-executor/0.3.12 \
    crate://crates.io/futures-macro/0.3.12 \
    crate://crates.io/futures-sink/0.3.12 \
    crate://crates.io/futures-task/0.3.12 \
    crate://crates.io/futures-util/0.3.12 \
    crate://crates.io/getrandom/0.2.2 \
    crate://crates.io/glib-macros/0.10.1 \
    crate://crates.io/glib-sys/0.10.1 \
    crate://crates.io/glib/0.10.3 \
    crate://crates.io/gobject-sys/0.10.0 \
    crate://crates.io/gstreamer-app-sys/0.9.1 \
    crate://crates.io/gstreamer-app/0.16.5 \
    crate://crates.io/gstreamer-base-sys/0.9.1 \
    crate://crates.io/gstreamer-base/0.16.5 \
    crate://crates.io/gstreamer-sys/0.9.1 \
    crate://crates.io/gstreamer-video-sys/0.9.1 \
    crate://crates.io/gstreamer-video/0.16.6 \
    crate://crates.io/gstreamer/0.16.5 \
    crate://crates.io/heck/0.3.2 \
    crate://crates.io/itertools/0.9.0 \
    crate://crates.io/libc/0.2.82 \
    crate://crates.io/libloading/0.6.7 \
    crate://crates.io/muldiv/0.2.1 \
    crate://crates.io/nix/0.18.0 \
    crate://crates.io/num-integer/0.1.44 \
    crate://crates.io/num-rational/0.3.2 \
    crate://crates.io/num-traits/0.2.14 \
    crate://crates.io/once_cell/1.5.2 \
    crate://crates.io/paste/1.0.4 \
    crate://crates.io/pin-project-lite/0.2.4 \
    crate://crates.io/pin-utils/0.1.0 \
    crate://crates.io/pkg-config/0.3.19 \
    crate://crates.io/ppv-lite86/0.2.10 \
    crate://crates.io/pretty-hex/0.2.1 \
    crate://crates.io/proc-macro-crate/0.1.5 \
    crate://crates.io/proc-macro-error-attr/1.0.4 \
    crate://crates.io/proc-macro-error/1.0.4 \
    crate://crates.io/proc-macro-hack/0.5.19 \
    crate://crates.io/proc-macro-nested/0.1.7 \
    crate://crates.io/proc-macro2/1.0.24 \
    crate://crates.io/quote/1.0.8 \
    crate://crates.io/rand/0.8.3 \
    crate://crates.io/rand_chacha/0.3.0 \
    crate://crates.io/rand_core/0.6.1 \
    crate://crates.io/rand_hc/0.3.0 \
    crate://crates.io/redox_syscall/0.2.4 \
    crate://crates.io/remove_dir_all/0.5.3 \
    crate://crates.io/scoped-tls/1.0.0 \
    crate://crates.io/serde/1.0.123 \
    crate://crates.io/slab/0.4.2 \
    crate://crates.io/smallvec/1.6.1 \
    crate://crates.io/strum/0.18.0 \
    crate://crates.io/strum_macros/0.18.0 \
    crate://crates.io/syn/1.0.60 \
    crate://crates.io/system-deps/1.3.2 \
    crate://crates.io/tempfile/3.2.0 \
    crate://crates.io/thiserror-impl/1.0.23 \
    crate://crates.io/thiserror/1.0.23 \
    crate://crates.io/toml/0.5.8 \
    crate://crates.io/unicode-segmentation/1.7.1 \
    crate://crates.io/unicode-xid/0.2.1 \
    crate://crates.io/version-compare/0.0.10 \
    crate://crates.io/version_check/0.9.2 \
    crate://crates.io/wasi/0.10.1+wasi-snapshot-preview1 \
    crate://crates.io/wayland-client/0.28.3 \
    crate://crates.io/wayland-commons/0.28.3 \
    crate://crates.io/wayland-protocols/0.28.3 \
    crate://crates.io/wayland-scanner/0.28.3 \
    crate://crates.io/wayland-sys/0.28.3 \
    crate://crates.io/winapi-i686-pc-windows-gnu/0.4.0 \
    crate://crates.io/winapi-x86_64-pc-windows-gnu/0.4.0 \
    crate://crates.io/winapi/0.3.9 \
    crate://crates.io/xml-rs/0.8.3 \
   "

BBCLASSEXTEND = ""
