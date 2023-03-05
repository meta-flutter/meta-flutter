#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "flutter_rust_bridge_example"
HOMEPAGE = "https://github.com/fzyzcjy/flutter_rust_bridge"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=480e9b5af92d888295493a5cc7f2238e"

SRCREV = "8c984efe63e0ed306cf2ab497788d4e94392e539"

SRC_URI += " \
    git://github.com/fzyzcjy/flutter_rust_bridge.git;lfs=0;branch=master;protocol=https;destsuffix=git \
    crate://crates.io/addr2line/0.16.0 \
    crate://crates.io/adler/1.0.2 \
    crate://crates.io/adler32/1.2.0 \
    crate://crates.io/allo-isolate/0.1.14-beta.1 \
    crate://crates.io/anyhow/1.0.64 \
    crate://crates.io/atomic/0.5.0 \
    crate://crates.io/autocfg/1.0.1 \
    crate://crates.io/backtrace/0.3.61 \
    crate://crates.io/bit_field/0.10.1 \
    crate://crates.io/bitflags/1.3.2 \
    crate://crates.io/build-target/0.4.0 \
    crate://crates.io/bumpalo/3.9.1 \
    crate://crates.io/bytemuck/1.11.0 \
    crate://crates.io/byteorder/1.4.3 \
    crate://crates.io/cc/1.0.70 \
    crate://crates.io/cfg-if/1.0.0 \
    crate://crates.io/color_quant/1.1.0 \
    crate://crates.io/console_error_panic_hook/0.1.7 \
    crate://crates.io/crc32fast/1.2.2 \
    crate://crates.io/crossbeam-channel/0.5.1 \
    crate://crates.io/crossbeam-deque/0.8.1 \
    crate://crates.io/crossbeam-epoch/0.9.5 \
    crate://crates.io/crossbeam-queue/0.3.2 \
    crate://crates.io/crossbeam-utils/0.8.8 \
    crate://crates.io/crossbeam/0.8.2 \
    crate://crates.io/deflate/0.9.1 \
    crate://crates.io/deflate/1.0.0 \
    crate://crates.io/either/1.6.1 \
    crate://crates.io/encoding-index-japanese/1.20141219.5 \
    crate://crates.io/encoding-index-korean/1.20141219.5 \
    crate://crates.io/encoding-index-simpchinese/1.20141219.5 \
    crate://crates.io/encoding-index-singlebyte/1.20141219.5 \
    crate://crates.io/encoding-index-tradchinese/1.20141219.5 \
    crate://crates.io/encoding/0.2.33 \
    crate://crates.io/encoding_index_tests/0.1.4 \
    crate://crates.io/exr/1.4.2 \
    crate://crates.io/flate2/1.0.22 \
    crate://crates.io/flume/0.10.10 \
    crate://crates.io/futures-core/0.3.19 \
    crate://crates.io/futures-sink/0.3.19 \
    crate://crates.io/getrandom/0.2.4 \
    crate://crates.io/gif/0.11.3 \
    crate://crates.io/gimli/0.25.0 \
    crate://crates.io/half/1.8.2 \
    crate://crates.io/hermit-abi/0.1.19 \
    crate://crates.io/image/0.24.3 \
    crate://crates.io/inflate/0.4.5 \
    crate://crates.io/jpeg-decoder/0.1.22 \
    crate://crates.io/jpeg-decoder/0.2.1 \
    crate://crates.io/js-sys/0.3.58 \
    crate://crates.io/lazy_static/1.4.0 \
    crate://crates.io/lebe/0.5.1 \
    crate://crates.io/libc/0.2.103 \
    crate://crates.io/lock_api/0.4.6 \
    crate://crates.io/log/0.4.14 \
    crate://crates.io/memchr/2.4.1 \
    crate://crates.io/memoffset/0.6.4 \
    crate://crates.io/miniz_oxide/0.4.4 \
    crate://crates.io/nanorand/0.6.1 \
    crate://crates.io/num-bigint/0.4.3 \
    crate://crates.io/num-complex/0.4.0 \
    crate://crates.io/num-integer/0.1.44 \
    crate://crates.io/num-iter/0.1.42 \
    crate://crates.io/num-rational/0.4.0 \
    crate://crates.io/num-traits/0.2.14 \
    crate://crates.io/num/0.4.0 \
    crate://crates.io/num_cpus/1.13.0 \
    crate://crates.io/object/0.26.2 \
    crate://crates.io/parking_lot/0.12.1 \
    crate://crates.io/parking_lot_core/0.9.0 \
    crate://crates.io/pin-project-internal/1.0.10 \
    crate://crates.io/pin-project/1.0.10 \
    crate://crates.io/png/0.17.2 \
    crate://crates.io/proc-macro2/1.0.36 \
    crate://crates.io/quote/1.0.15 \
    crate://crates.io/rayon-core/1.9.1 \
    crate://crates.io/rayon/1.5.1 \
    crate://crates.io/redox_syscall/0.2.10 \
    crate://crates.io/rustc-demangle/0.1.21 \
    crate://crates.io/scoped_threadpool/0.1.9 \
    crate://crates.io/scopeguard/1.1.0 \
    crate://crates.io/smallvec/1.7.0 \
    crate://crates.io/spin/0.9.2 \
    crate://crates.io/syn/1.0.86 \
    crate://crates.io/threadpool/1.8.1 \
    crate://crates.io/tiff/0.7.1 \
    crate://crates.io/unicode-xid/0.2.2 \
    crate://crates.io/wasi/0.10.2+wasi-snapshot-preview1 \
    crate://crates.io/wasm-bindgen-backend/0.2.81 \
    crate://crates.io/wasm-bindgen-macro-support/0.2.81 \
    crate://crates.io/wasm-bindgen-macro/0.2.81 \
    crate://crates.io/wasm-bindgen-shared/0.2.81 \
    crate://crates.io/wasm-bindgen/0.2.81 \
    crate://crates.io/web-sys/0.3.58 \
    crate://crates.io/weezl/0.1.5 \
    crate://crates.io/windows-sys/0.29.0 \
    crate://crates.io/windows_aarch64_msvc/0.29.0 \
    crate://crates.io/windows_i686_gnu/0.29.0 \
    crate://crates.io/windows_i686_msvc/0.29.0 \
    crate://crates.io/windows_x86_64_gnu/0.29.0 \
    crate://crates.io/windows_x86_64_msvc/0.29.0 \
"

S = "${WORKDIR}/git"

CARGO_SRC_DIR = "frb_example/with_flutter/rust"


inherit cargo
