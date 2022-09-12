SUMMARY = "Membrane Example shared module"
HOMEPAGE = "https://github.com/jerel/membrane"
LICENSE = "Apache-2.0"

LIC_FILES_CHKSUM = "\
    file://${COREBASE}/meta/files/common-licenses/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10 \
"

DEPENDS += "\
    membrane-example-native \
    "

DEPENDS:class-native += "\
    flutter-sdk-native \
    compiler-rt-native \
    libcxx-native \
    "

SRCREV = "4e788100fc71162ee4ac427620cc96e62c410bba"
PV:append = ".AUTOINC+4e788100fc"

SRC_URI += " \
    git://github.com/jerel/membrane.git;protocol=https;branch=main \
    crate://crates.io/addr2line/0.17.0 \
    crate://crates.io/adler/1.0.2 \
    crate://crates.io/aho-corasick/0.7.18 \
    crate://crates.io/allo-isolate/0.1.12 \
    crate://crates.io/ansi_term/0.12.1 \
    crate://crates.io/anyhow/1.0.57 \
    crate://crates.io/async-stream-impl/0.3.3 \
    crate://crates.io/async-stream/0.3.3 \
    crate://crates.io/atomic/0.5.1 \
    crate://crates.io/atty/0.2.14 \
    crate://crates.io/autocfg/1.1.0 \
    crate://crates.io/backtrace/0.3.65 \
    crate://crates.io/bcs/0.1.3 \
    crate://crates.io/bincode/1.3.3 \
    crate://crates.io/bitflags/1.3.2 \
    crate://crates.io/bytes/1.1.0 \
    crate://crates.io/cc/1.0.73 \
    crate://crates.io/cfg-if/1.0.0 \
    crate://crates.io/clap/2.34.0 \
    crate://crates.io/ctor/0.1.22 \
    crate://crates.io/failure/0.1.8 \
    crate://crates.io/failure_derive/0.1.8 \
    crate://crates.io/ffi_helpers/0.2.0 \
    crate://crates.io/futures-channel/0.3.21 \
    crate://crates.io/futures-core/0.3.21 \
    crate://crates.io/futures-executor/0.3.21 \
    crate://crates.io/futures-io/0.3.21 \
    crate://crates.io/futures-macro/0.3.21 \
    crate://crates.io/futures-sink/0.3.21 \
    crate://crates.io/futures-task/0.3.21 \
    crate://crates.io/futures-util/0.3.21 \
    crate://crates.io/futures/0.3.21 \
    crate://crates.io/getrandom/0.2.6 \
    crate://crates.io/ghost/0.1.4 \
    crate://crates.io/gimli/0.26.1 \
    crate://crates.io/glob/0.3.0 \
    crate://crates.io/hashbrown/0.11.2 \
    crate://crates.io/heck/0.3.3 \
    crate://crates.io/hermit-abi/0.1.19 \
    crate://crates.io/include_dir/0.6.2 \
    crate://crates.io/include_dir_impl/0.6.2 \
    crate://crates.io/indexmap/1.8.2 \
    crate://crates.io/inventory-impl/0.1.11 \
    crate://crates.io/inventory/0.1.11 \
    crate://crates.io/lazy_static/1.4.0 \
    crate://crates.io/libc/0.2.126 \
    crate://crates.io/linked-hash-map/0.5.4 \
    crate://crates.io/lock_api/0.4.7 \
    crate://crates.io/log/0.4.17 \
    crate://crates.io/maplit/1.0.2 \
    crate://crates.io/memchr/2.5.0 \
    crate://crates.io/miniz_oxide/0.5.3 \
    crate://crates.io/mio/0.8.3 \
    crate://crates.io/num_cpus/1.13.1 \
    crate://crates.io/object/0.28.4 \
    crate://crates.io/once_cell/1.12.0 \
    crate://crates.io/parking_lot/0.12.1 \
    crate://crates.io/parking_lot_core/0.9.3 \
    crate://crates.io/phf/0.10.1 \
    crate://crates.io/phf_generator/0.10.0 \
    crate://crates.io/phf_macros/0.10.0 \
    crate://crates.io/phf_shared/0.10.0 \
    crate://crates.io/pin-project-lite/0.2.9 \
    crate://crates.io/pin-utils/0.1.0 \
    crate://crates.io/ppv-lite86/0.2.16 \
    crate://crates.io/proc-macro-error-attr/1.0.4 \
    crate://crates.io/proc-macro-error/1.0.4 \
    crate://crates.io/proc-macro-hack/0.5.19 \
    crate://crates.io/proc-macro2/1.0.39 \
    crate://crates.io/quote/1.0.18 \
    crate://crates.io/rand/0.8.5 \
    crate://crates.io/rand_chacha/0.3.1 \
    crate://crates.io/rand_core/0.6.3 \
    crate://crates.io/redox_syscall/0.2.13 \
    crate://crates.io/regex-syntax/0.6.26 \
    crate://crates.io/regex/1.5.6 \
    crate://crates.io/rustc-demangle/0.1.21 \
    crate://crates.io/ryu/1.0.10 \
    crate://crates.io/scopeguard/1.1.0 \
    crate://crates.io/serde-generate/0.24.0 \
    crate://crates.io/serde-reflection/0.3.6 \
    crate://crates.io/serde/1.0.137 \
    crate://crates.io/serde_bytes/0.11.6 \
    crate://crates.io/serde_derive/1.0.137 \
    crate://crates.io/serde_yaml/0.8.24 \
    crate://crates.io/signal-hook-registry/1.4.0 \
    crate://crates.io/siphasher/0.3.10 \
    crate://crates.io/slab/0.4.6 \
    crate://crates.io/smallvec/1.8.0 \
    crate://crates.io/smawk/0.3.1 \
    crate://crates.io/socket2/0.4.4 \
    crate://crates.io/strsim/0.8.0 \
    crate://crates.io/structopt-derive/0.4.18 \
    crate://crates.io/structopt/0.3.26 \
    crate://crates.io/syn/1.0.96 \
    crate://crates.io/synstructure/0.12.6 \
    crate://crates.io/textwrap/0.11.0 \
    crate://crates.io/textwrap/0.13.4 \
    crate://crates.io/thiserror-impl/1.0.31 \
    crate://crates.io/thiserror/1.0.31 \
    crate://crates.io/tokio-macros/1.8.0 \
    crate://crates.io/tokio-stream/0.1.9 \
    crate://crates.io/tokio/1.19.2 \
    crate://crates.io/unicode-ident/1.0.0 \
    crate://crates.io/unicode-segmentation/1.9.0 \
    crate://crates.io/unicode-width/0.1.9 \
    crate://crates.io/unicode-xid/0.2.3 \
    crate://crates.io/vec_map/0.8.2 \
    crate://crates.io/version_check/0.9.4 \
    crate://crates.io/wasi/0.10.2+wasi-snapshot-preview1 \
    crate://crates.io/wasi/0.11.0+wasi-snapshot-preview1 \
    crate://crates.io/winapi-i686-pc-windows-gnu/0.4.0 \
    crate://crates.io/winapi-x86_64-pc-windows-gnu/0.4.0 \
    crate://crates.io/winapi/0.3.9 \
    crate://crates.io/windows-sys/0.36.1 \
    crate://crates.io/windows_aarch64_msvc/0.36.1 \
    crate://crates.io/windows_i686_gnu/0.36.1 \
    crate://crates.io/windows_i686_msvc/0.36.1 \
    crate://crates.io/windows_x86_64_gnu/0.36.1 \
    crate://crates.io/windows_x86_64_msvc/0.36.1 \
    crate://crates.io/yaml-rust/0.4.5 \
"

S = "${WORKDIR}/git"

CARGO_SRC_DIR = "example"

RUNTIME:class-native = "llvm"
TOOLCHAIN:class-native = "clang"
PREFERRED_PROVIDER:libgcc:class-native = "compiler-rt"

inherit cargo

#
# Dart generation
#
cargo_do_compile:class-native() {    

    export RUSTFLAGS
    export RUST_BACKTRACE=full
    export CARGO_TARGET_DIR=${S}/target
    export CARGO_BUILD_TARGET="${HOST_SYS}"

    export PUB_CACHE=${S}/dart_example/pub_cache
    export MEMBRANE_LLVM_PATHS=${STAGING_DIR_NATIVE}/usr
    export PATH="${STAGING_DIR_NATIVE}/usr/share/flutter/sdk/bin:$PATH"

    cd ${S}/${CARGO_SRC_DIR}

    # requires Dart
    bbnote `dart --version`

    # requires libclang.so
    bbnote `find ${STAGING_DIR_NATIVE} -iname libclang.so*`

    "${CARGO}" run
}

cargo_do_install:class-native() {

    install -d ${D}${datadir}/membrane/dart_example

    cp -r ${S}/dart_example/* ${D}${datadir}/membrane/dart_example
}

cargo_do_install:append:class-target() {

    rm -rf ${D}${bindir}
}

FILES:${PN}:class-native = "${datadir}"

FILES:${PN} = "${libdir}"

BBCLASSEXTEND += "native"
