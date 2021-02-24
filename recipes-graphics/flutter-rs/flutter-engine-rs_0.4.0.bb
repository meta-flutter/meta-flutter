SUMMARY = "flutter-rs engine"
HOMEPAGE = "https://github.com/flutter-rs/flutter-rs"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=310e426952cc9a0497bbbfe0faa269bc"

DEPENDS += "clang-native"

PV_append = ".69c6706d15"
SRC_URI = "git://github.com/flutter-rs/flutter-rs.git;protocol=https;nobranch=1"
SRCREV = "69c6706d15f256beba5d99e94124c70c5b27e7d1"
S = "${WORKDIR}/git"

CARGO_SRC_DIR = "flutter-engine"

SRC_URI_append = " \
    crate://crates.io/adler32/1.0.4 \
    crate://crates.io/aho-corasick/0.7.10 \
    crate://crates.io/ansi_term/0.11.0 \
    crate://crates.io/arrayref/0.3.6 \
    crate://crates.io/arrayvec/0.5.1 \
    crate://crates.io/async-std/1.5.0 \
    crate://crates.io/async-task/1.3.1 \
    crate://crates.io/atty/0.2.14 \
    crate://crates.io/autocfg/1.0.0 \
    crate://crates.io/backtrace-sys/0.1.37 \
    crate://crates.io/backtrace/0.3.46 \
    crate://crates.io/base64/0.11.0 \
    crate://crates.io/bindgen/0.52.0 \
    crate://crates.io/bitflags/1.2.1 \
    crate://crates.io/blake2b_simd/0.5.10 \
    crate://crates.io/block/0.1.6 \
    crate://crates.io/byteorder/1.3.4 \
    crate://crates.io/bzip2-sys/0.1.8+1.0.8 \
    crate://crates.io/bzip2/0.3.3 \
    crate://crates.io/cc/1.0.52 \
    crate://crates.io/cexpr/0.3.6 \
    crate://crates.io/cfg-if/0.1.10 \
    crate://crates.io/clang-sys/0.28.1 \
    crate://crates.io/clap/2.33.0 \
    crate://crates.io/cloudabi/0.0.3 \
    crate://crates.io/cmake/0.1.42 \
    crate://crates.io/console/0.11.2 \
    crate://crates.io/constant_time_eq/0.1.5 \
    crate://crates.io/crc32fast/1.2.0 \
    crate://crates.io/crossbeam-channel/0.4.2 \
    crate://crates.io/crossbeam-deque/0.7.3 \
    crate://crates.io/crossbeam-epoch/0.8.2 \
    crate://crates.io/crossbeam-utils/0.7.2 \
    crate://crates.io/curl-sys/0.4.31+curl-7.70.0 \
    crate://crates.io/curl/0.4.29 \
    crate://crates.io/dirs-sys/0.3.4 \
    crate://crates.io/dirs/2.0.2 \
    crate://crates.io/encode_unicode/0.3.6 \
    crate://crates.io/env_logger/0.7.1 \
    crate://crates.io/failure/0.1.8 \
    crate://crates.io/flate2/1.0.14 \
    crate://crates.io/fuchsia-zircon-sys/0.3.3 \
    crate://crates.io/fuchsia-zircon/0.3.3 \
    crate://crates.io/futures-core/0.3.4 \
    crate://crates.io/futures-io/0.3.4 \
    crate://crates.io/futures-task/0.3.4 \
    crate://crates.io/futures-timer/2.0.2 \
    crate://crates.io/getrandom/0.1.14 \
    crate://crates.io/gl/0.14.0 \
    crate://crates.io/gl_generator/0.14.0 \
    crate://crates.io/glfw-sys/3.3.2 \
    crate://crates.io/glfw/0.37.0 \
    crate://crates.io/glob/0.3.0 \
    crate://crates.io/hermit-abi/0.1.12 \
    crate://crates.io/humantime/1.3.0 \
    crate://crates.io/image/0.22.5 \
    crate://crates.io/indexmap/1.3.2 \
    crate://crates.io/indicatif/0.14.0 \
    crate://crates.io/iovec/0.1.4 \
    crate://crates.io/itoa/0.4.5 \
    crate://crates.io/kernel32-sys/0.2.2 \
    crate://crates.io/khronos_api/3.1.0 \
    crate://crates.io/kv-log-macro/1.0.4 \
    crate://crates.io/lazy_static/1.4.0 \
    crate://crates.io/lazycell/1.2.1 \
    crate://crates.io/libc/0.2.69 \
    crate://crates.io/libloading/0.5.2 \
    crate://crates.io/libz-sys/1.0.25 \
    crate://crates.io/locale_config/0.3.0 \
    crate://crates.io/lock_api/0.3.4 \
    crate://crates.io/log/0.4.8 \
    crate://crates.io/malloc_buf/0.0.6 \
    crate://crates.io/maybe-uninit/2.0.0 \
    crate://crates.io/memchr/2.3.3 \
    crate://crates.io/memoffset/0.5.4 \
    crate://crates.io/miniz_oxide/0.3.6 \
    crate://crates.io/mio-uds/0.6.8 \
    crate://crates.io/mio/0.6.22 \
    crate://crates.io/miow/0.2.1 \
    crate://crates.io/net2/0.2.34 \
    crate://crates.io/nom/4.2.3 \
    crate://crates.io/num-integer/0.1.42 \
    crate://crates.io/num-iter/0.1.40 \
    crate://crates.io/num-rational/0.2.4 \
    crate://crates.io/num-traits/0.2.11 \
    crate://crates.io/num_cpus/1.13.0 \
    crate://crates.io/number_prefix/0.3.0 \
    crate://crates.io/objc-foundation/0.1.1 \
    crate://crates.io/objc/0.2.7 \
    crate://crates.io/objc_id/0.1.1 \
    crate://crates.io/once_cell/1.3.1 \
    crate://crates.io/openssl-probe/0.1.2 \
    crate://crates.io/openssl-sys/0.9.55 \
    crate://crates.io/parking_lot/0.10.2 \
    crate://crates.io/parking_lot_core/0.7.2 \
    crate://crates.io/peeking_take_while/0.1.2 \
    crate://crates.io/pin-project-lite/0.1.5 \
    crate://crates.io/pin-utils/0.1.0 \
    crate://crates.io/pkg-config/0.3.17 \
    crate://crates.io/podio/0.1.6 \
    crate://crates.io/priority-queue/0.7.0 \
    crate://crates.io/proc-macro2/1.0.12 \
    crate://crates.io/quick-error/1.2.3 \
    crate://crates.io/quote/1.0.4 \
    crate://crates.io/raw-window-handle/0.3.3 \
    crate://crates.io/redox_syscall/0.1.56 \
    crate://crates.io/redox_users/0.3.4 \
    crate://crates.io/regex-syntax/0.6.17 \
    crate://crates.io/regex/1.3.7 \
    crate://crates.io/rust-argon2/0.7.0 \
    crate://crates.io/rustc-demangle/0.1.16 \
    crate://crates.io/rustc-hash/1.1.0 \
    crate://crates.io/ryu/1.0.4 \
    crate://crates.io/schannel/0.1.18 \
    crate://crates.io/scopeguard/1.1.0 \
    crate://crates.io/semver-parser/0.7.0 \
    crate://crates.io/semver/0.9.0 \
    crate://crates.io/serde/1.0.106 \
    crate://crates.io/serde_derive/1.0.106 \
    crate://crates.io/serde_json/1.0.52 \
    crate://crates.io/shlex/0.1.1 \
    crate://crates.io/slab/0.4.2 \
    crate://crates.io/smallvec/1.4.0 \
    crate://crates.io/socket2/0.3.12 \
    crate://crates.io/strsim/0.8.0 \
    crate://crates.io/syn/1.0.19 \
    crate://crates.io/take_mut/0.2.2 \
    crate://crates.io/termcolor/1.1.0 \
    crate://crates.io/terminal_size/0.1.12 \
    crate://crates.io/termios/0.3.2 \
    crate://crates.io/textwrap/0.11.0 \
    crate://crates.io/thread_local/1.0.1 \
    crate://crates.io/time/0.1.43 \
    crate://crates.io/tinyfiledialogs/3.3.9 \
    crate://crates.io/tinystr/0.3.2 \
    crate://crates.io/unic-langid-impl/0.7.2 \
    crate://crates.io/unic-locale-impl/0.7.1 \
    crate://crates.io/unic-locale/0.7.1 \
    crate://crates.io/unicode-width/0.1.7 \
    crate://crates.io/unicode-xid/0.2.0 \
    crate://crates.io/vcpkg/0.2.8 \
    crate://crates.io/vec_map/0.8.2 \
    crate://crates.io/version_check/0.1.5 \
    crate://crates.io/wasi/0.9.0+wasi-snapshot-preview1 \
    crate://crates.io/which/3.1.1 \
    crate://crates.io/winapi-build/0.1.1 \
    crate://crates.io/winapi-i686-pc-windows-gnu/0.4.0 \
    crate://crates.io/winapi-util/0.1.5 \
    crate://crates.io/winapi-x86_64-pc-windows-gnu/0.4.0 \
    crate://crates.io/winapi/0.2.8 \
    crate://crates.io/winapi/0.3.8 \
    crate://crates.io/ws2_32-sys/0.2.1 \
    crate://crates.io/xml-rs/0.8.3 \
    crate://crates.io/zip/0.5.5 \
"

inherit cargo

BBCLASSEXTEND = ""
