DEPENDS += "\
    flutter-sdk-native \
    compiler-rt \
    libcxx \
    "

RDEPENDS:${PN} += "\
    bash \
    "

RUNTIME = "llvm"
TOOLCHAIN = "clang"
PREFERRED_PROVIDER:libgcc = "compiler-rt"

FLUTTER_SDK = "${STAGING_DIR_NATIVE}/usr/share/flutter/sdk"
PUB_CACHE = "${S}/dart_example/pub_cache"

EXAMPLE_INSTALL_DIR = "${datadir}/membrane/dart_example"

cargo_do_compile:prepend() {
    export PATH=${FLUTTER_SDK}/bin:$PATH
    export PUB_CACHE=${PUB_CACHE}
    export MEMBRANE_LLVM_PATHS=${STAGING_DIR_NATIVE}/usr

    cd ${S}/example
    cargo run
}

cargo_do_compile:append() {
    cd ${S}/dart_example

    dart pub get --directory=${S}/dart_example --no-offline
    dart pub get --directory=${S}/dart_example --offline
}

cargo_do_install:append() {

    install -d ${D}${EXAMPLE_INSTALL_DIR}

    rm ${D}${bindir}/generator
    rm -rf ${D}${bindir}

    mv ${D}${libdir}/rust/libexample.so ${D}${EXAMPLE_INSTALL_DIR}/
    rm -rf ${D}${libdir}/rust
    rm -rf ${D}${libdir}

    cp -r ${S}/dart_example/* ${D}${EXAMPLE_INSTALL_DIR}/
}

FILES:${PN} = "${datadir}"
