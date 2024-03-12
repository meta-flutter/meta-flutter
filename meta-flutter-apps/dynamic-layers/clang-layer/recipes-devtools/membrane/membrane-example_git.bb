#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

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

SRCREV = "402133efc123fbf86997118209b3a84007e4000d"
PV:append = ".AUTOINC+402133efc1"
SRC_URI = "git://github.com/jerel/membrane_template.git;lfs=0;branch=main;protocol=https;destsuffix=git"

SRC_URI += " \
    crate://crates.io/addr2line/0.17.0 \
    crate://crates.io/adler/1.0.2 \
    crate://crates.io/aho-corasick/0.7.18 \
    crate://crates.io/allo-isolate/0.1.13 \
    crate://crates.io/ansi_term/0.12.1 \
    crate://crates.io/anyhow/1.0.60 \
    crate://crates.io/async-stream/0.3.3 \
    crate://crates.io/async-stream-impl/0.3.3 \
    crate://crates.io/atomic/0.5.1 \
    crate://crates.io/atty/0.2.14 \
    crate://crates.io/autocfg/1.1.0 \
    crate://crates.io/backtrace/0.3.66 \
    crate://crates.io/bcs/0.1.3 \
    crate://crates.io/bincode/1.3.3 \
    crate://crates.io/bitflags/1.3.2 \
    crate://crates.io/cc/1.0.73 \
    crate://crates.io/cfg-if/1.0.0 \
    crate://crates.io/clap/2.34.0 \
    crate://crates.io/ctor/0.1.23 \
    crate://crates.io/failure/0.1.8 \
    crate://crates.io/failure_derive/0.1.8 \
    crate://crates.io/ffi_helpers/0.2.0 \
    crate://crates.io/futures/0.3.21 \
    crate://crates.io/futures-channel/0.3.21 \
    crate://crates.io/futures-core/0.3.21 \
    crate://crates.io/futures-executor/0.3.21 \
    crate://crates.io/futures-io/0.3.21 \
    crate://crates.io/futures-macro/0.3.21 \
    crate://crates.io/futures-sink/0.3.21 \
    crate://crates.io/futures-task/0.3.21 \
    crate://crates.io/futures-util/0.3.21 \
    crate://crates.io/getrandom/0.2.7 \
    crate://crates.io/ghost/0.1.6 \
    crate://crates.io/gimli/0.26.2 \
    crate://crates.io/glob/0.3.0 \
    crate://crates.io/hashbrown/0.12.3 \
    crate://crates.io/heck/0.3.3 \
    crate://crates.io/hermit-abi/0.1.19 \
    crate://crates.io/include_dir/0.6.2 \
    crate://crates.io/include_dir_impl/0.6.2 \
    crate://crates.io/indexmap/1.9.1 \
    crate://crates.io/inventory/0.1.11 \
    crate://crates.io/inventory-impl/0.1.11 \
    crate://crates.io/lazy_static/1.4.0 \
    crate://crates.io/libc/0.2.129 \
    crate://crates.io/linked-hash-map/0.5.6 \
    crate://crates.io/maplit/1.0.2 \
    crate://crates.io/memchr/2.5.0 \
    crate://crates.io/miniz_oxide/0.5.3 \
    crate://crates.io/num_cpus/1.13.1 \
    crate://crates.io/object/0.29.0 \
    crate://crates.io/once_cell/1.13.0 \
    crate://crates.io/phf/0.10.1 \
    crate://crates.io/phf_generator/0.10.0 \
    crate://crates.io/phf_macros/0.10.0 \
    crate://crates.io/phf_shared/0.10.0 \
    crate://crates.io/pin-project-lite/0.2.9 \
    crate://crates.io/pin-utils/0.1.0 \
    crate://crates.io/ppv-lite86/0.2.16 \
    crate://crates.io/proc-macro-error/1.0.4 \
    crate://crates.io/proc-macro-error-attr/1.0.4 \
    crate://crates.io/proc-macro-hack/0.5.19 \
    crate://crates.io/proc-macro2/1.0.43 \
    crate://crates.io/quote/1.0.21 \
    crate://crates.io/rand/0.8.5 \
    crate://crates.io/rand_chacha/0.3.1 \
    crate://crates.io/rand_core/0.6.3 \
    crate://crates.io/regex/1.6.0 \
    crate://crates.io/regex-syntax/0.6.27 \
    crate://crates.io/rustc-demangle/0.1.21 \
    crate://crates.io/ryu/1.0.11 \
    crate://crates.io/serde/1.0.143 \
    crate://crates.io/serde-generate/0.24.0 \
    crate://crates.io/serde-reflection/0.3.6 \
    crate://crates.io/serde_bytes/0.11.7 \
    crate://crates.io/serde_derive/1.0.143 \
    crate://crates.io/serde_yaml/0.8.26 \
    crate://crates.io/siphasher/0.3.10 \
    crate://crates.io/slab/0.4.7 \
    crate://crates.io/smawk/0.3.1 \
    crate://crates.io/strsim/0.8.0 \
    crate://crates.io/structopt/0.3.26 \
    crate://crates.io/structopt-derive/0.4.18 \
    crate://crates.io/syn/1.0.99 \
    crate://crates.io/synstructure/0.12.6 \
    crate://crates.io/textwrap/0.11.0 \
    crate://crates.io/textwrap/0.13.4 \
    crate://crates.io/thiserror/1.0.32 \
    crate://crates.io/thiserror-impl/1.0.32 \
    crate://crates.io/tokio/1.20.1 \
    crate://crates.io/unicode-ident/1.0.3 \
    crate://crates.io/unicode-segmentation/1.9.0 \
    crate://crates.io/unicode-width/0.1.9 \
    crate://crates.io/unicode-xid/0.2.3 \
    crate://crates.io/vec_map/0.8.2 \
    crate://crates.io/version_check/0.9.4 \
    crate://crates.io/wasi/0.11.0+wasi-snapshot-preview1 \
    crate://crates.io/winapi/0.3.9 \
    crate://crates.io/winapi-i686-pc-windows-gnu/0.4.0 \
    crate://crates.io/winapi-x86_64-pc-windows-gnu/0.4.0 \
    crate://crates.io/yaml-rust/0.4.5 \
    git://github.com/jerel/membrane;lfs=0;nobranch=1;protocol=https;destsuffix=membrane;name=membrane \
"

SRCREV_FORMAT .= "_membrane"
SRCREV_membrane = "0ad0fc8907c93e087e01598260bee1863f5adc02"

EXTRA_OECARGO_PATHS += "\
    ${WORKDIR}/membrane \
"

SRC_URI[addr2line-0.17.0.sha256sum] = "b9ecd88a8c8378ca913a680cd98f0f13ac67383d35993f86c90a70e3f137816b"
SRC_URI[adler-1.0.2.sha256sum] = "f26201604c87b1e01bd3d98f8d5d9a8fcbb815e8cedb41ffccbeb4bf593a35fe"
SRC_URI[aho-corasick-0.7.18.sha256sum] = "1e37cfd5e7657ada45f742d6e99ca5788580b5c529dc78faf11ece6dc702656f"
SRC_URI[allo-isolate-0.1.13.sha256sum] = "ccb993621e6bf1b67591005b0adad126159a0ab31af379743906158aed5330d0"
SRC_URI[ansi_term-0.12.1.sha256sum] = "d52a9bb7ec0cf484c551830a7ce27bd20d67eac647e1befb56b0be4ee39a55d2"
SRC_URI[anyhow-1.0.60.sha256sum] = "c794e162a5eff65c72ef524dfe393eb923c354e350bb78b9c7383df13f3bc142"
SRC_URI[async-stream-0.3.3.sha256sum] = "dad5c83079eae9969be7fadefe640a1c566901f05ff91ab221de4b6f68d9507e"
SRC_URI[async-stream-impl-0.3.3.sha256sum] = "10f203db73a71dfa2fb6dd22763990fa26f3d2625a6da2da900d23b87d26be27"
SRC_URI[atomic-0.5.1.sha256sum] = "b88d82667eca772c4aa12f0f1348b3ae643424c8876448f3f7bd5787032e234c"
SRC_URI[atty-0.2.14.sha256sum] = "d9b39be18770d11421cdb1b9947a45dd3f37e93092cbf377614828a319d5fee8"
SRC_URI[autocfg-1.1.0.sha256sum] = "d468802bab17cbc0cc575e9b053f41e72aa36bfa6b7f55e3529ffa43161b97fa"
SRC_URI[backtrace-0.3.66.sha256sum] = "cab84319d616cfb654d03394f38ab7e6f0919e181b1b57e1fd15e7fb4077d9a7"
SRC_URI[bcs-0.1.3.sha256sum] = "510fd83e3eaf7263b06182f3550b4c0af2af42cb36ab8024969ff5ea7fcb2833"
SRC_URI[bincode-1.3.3.sha256sum] = "b1f45e9417d87227c7a56d22e471c6206462cba514c7590c09aff4cf6d1ddcad"
SRC_URI[bitflags-1.3.2.sha256sum] = "bef38d45163c2f1dde094a7dfd33ccf595c92905c8f8f4fdc18d06fb1037718a"
SRC_URI[cc-1.0.73.sha256sum] = "2fff2a6927b3bb87f9595d67196a70493f627687a71d87a0d692242c33f58c11"
SRC_URI[cfg-if-1.0.0.sha256sum] = "baf1de4339761588bc0619e3cbc0120ee582ebb74b53b4efbf79117bd2da40fd"
SRC_URI[clap-2.34.0.sha256sum] = "a0610544180c38b88101fecf2dd634b174a62eef6946f84dfc6a7127512b381c"
SRC_URI[ctor-0.1.23.sha256sum] = "cdffe87e1d521a10f9696f833fe502293ea446d7f256c06128293a4119bdf4cb"
SRC_URI[failure-0.1.8.sha256sum] = "d32e9bd16cc02eae7db7ef620b392808b89f6a5e16bb3497d159c6b92a0f4f86"
SRC_URI[failure_derive-0.1.8.sha256sum] = "aa4da3c766cd7a0db8242e326e9e4e081edd567072893ed320008189715366a4"
SRC_URI[ffi_helpers-0.2.0.sha256sum] = "36a21261040ffbc3999698b39170d6e951f2823dd9ad7b50469309156060a50c"
SRC_URI[futures-0.3.21.sha256sum] = "f73fe65f54d1e12b726f517d3e2135ca3125a437b6d998caf1962961f7172d9e"
SRC_URI[futures-channel-0.3.21.sha256sum] = "c3083ce4b914124575708913bca19bfe887522d6e2e6d0952943f5eac4a74010"
SRC_URI[futures-core-0.3.21.sha256sum] = "0c09fd04b7e4073ac7156a9539b57a484a8ea920f79c7c675d05d289ab6110d3"
SRC_URI[futures-executor-0.3.21.sha256sum] = "9420b90cfa29e327d0429f19be13e7ddb68fa1cccb09d65e5706b8c7a749b8a6"
SRC_URI[futures-io-0.3.21.sha256sum] = "fc4045962a5a5e935ee2fdedaa4e08284547402885ab326734432bed5d12966b"
SRC_URI[futures-macro-0.3.21.sha256sum] = "33c1e13800337f4d4d7a316bf45a567dbcb6ffe087f16424852d97e97a91f512"
SRC_URI[futures-sink-0.3.21.sha256sum] = "21163e139fa306126e6eedaf49ecdb4588f939600f0b1e770f4205ee4b7fa868"
SRC_URI[futures-task-0.3.21.sha256sum] = "57c66a976bf5909d801bbef33416c41372779507e7a6b3a5e25e4749c58f776a"
SRC_URI[futures-util-0.3.21.sha256sum] = "d8b7abd5d659d9b90c8cba917f6ec750a74e2dc23902ef9cd4cc8c8b22e6036a"
SRC_URI[getrandom-0.2.7.sha256sum] = "4eb1a864a501629691edf6c15a593b7a51eebaa1e8468e9ddc623de7c9b58ec6"
SRC_URI[ghost-0.1.6.sha256sum] = "eb19fe8de3ea0920d282f7b77dd4227aea6b8b999b42cdf0ca41b2472b14443a"
SRC_URI[gimli-0.26.2.sha256sum] = "22030e2c5a68ec659fde1e949a745124b48e6fa8b045b7ed5bd1fe4ccc5c4e5d"
SRC_URI[glob-0.3.0.sha256sum] = "9b919933a397b79c37e33b77bb2aa3dc8eb6e165ad809e58ff75bc7db2e34574"
SRC_URI[hashbrown-0.12.3.sha256sum] = "8a9ee70c43aaf417c914396645a0fa852624801b24ebb7ae78fe8272889ac888"
SRC_URI[heck-0.3.3.sha256sum] = "6d621efb26863f0e9924c6ac577e8275e5e6b77455db64ffa6c65c904e9e132c"
SRC_URI[hermit-abi-0.1.19.sha256sum] = "62b467343b94ba476dcb2500d242dadbb39557df889310ac77c5d99100aaac33"
SRC_URI[include_dir-0.6.2.sha256sum] = "24b56e147e6187d61e9d0f039f10e070d0c0a887e24fe0bb9ca3f29bfde62cab"
SRC_URI[include_dir_impl-0.6.2.sha256sum] = "0a0c890c85da4bab7bce4204c707396bbd3c6c8a681716a51c8814cfc2b682df"
SRC_URI[indexmap-1.9.1.sha256sum] = "10a35a97730320ffe8e2d410b5d3b69279b98d2c14bdb8b70ea89ecf7888d41e"
SRC_URI[inventory-0.1.11.sha256sum] = "f0eb5160c60ba1e809707918ee329adb99d222888155835c6feedba19f6c3fd4"
SRC_URI[inventory-impl-0.1.11.sha256sum] = "7e41b53715c6f0c4be49510bb82dee2c1e51c8586d885abe65396e82ed518548"
SRC_URI[lazy_static-1.4.0.sha256sum] = "e2abad23fbc42b3700f2f279844dc832adb2b2eb069b2df918f455c4e18cc646"
SRC_URI[libc-0.2.129.sha256sum] = "64de3cc433455c14174d42e554d4027ee631c4d046d43e3ecc6efc4636cdc7a7"
SRC_URI[linked-hash-map-0.5.6.sha256sum] = "0717cef1bc8b636c6e1c1bbdefc09e6322da8a9321966e8928ef80d20f7f770f"
SRC_URI[maplit-1.0.2.sha256sum] = "3e2e65a1a2e43cfcb47a895c4c8b10d1f4a61097f9f254f183aee60cad9c651d"
SRC_URI[memchr-2.5.0.sha256sum] = "2dffe52ecf27772e601905b7522cb4ef790d2cc203488bbd0e2fe85fcb74566d"
SRC_URI[miniz_oxide-0.5.3.sha256sum] = "6f5c75688da582b8ffc1f1799e9db273f32133c49e048f614d22ec3256773ccc"
SRC_URI[num_cpus-1.13.1.sha256sum] = "19e64526ebdee182341572e50e9ad03965aa510cd94427a4549448f285e957a1"
SRC_URI[object-0.29.0.sha256sum] = "21158b2c33aa6d4561f1c0a6ea283ca92bc54802a93b263e910746d679a7eb53"
SRC_URI[once_cell-1.13.0.sha256sum] = "18a6dbe30758c9f83eb00cbea4ac95966305f5a7772f3f42ebfc7fc7eddbd8e1"
SRC_URI[phf-0.10.1.sha256sum] = "fabbf1ead8a5bcbc20f5f8b939ee3f5b0f6f281b6ad3468b84656b658b455259"
SRC_URI[phf_generator-0.10.0.sha256sum] = "5d5285893bb5eb82e6aaf5d59ee909a06a16737a8970984dd7746ba9283498d6"
SRC_URI[phf_macros-0.10.0.sha256sum] = "58fdf3184dd560f160dd73922bea2d5cd6e8f064bf4b13110abd81b03697b4e0"
SRC_URI[phf_shared-0.10.0.sha256sum] = "b6796ad771acdc0123d2a88dc428b5e38ef24456743ddb1744ed628f9815c096"
SRC_URI[pin-project-lite-0.2.9.sha256sum] = "e0a7ae3ac2f1173085d398531c705756c94a4c56843785df85a60c1a0afac116"
SRC_URI[pin-utils-0.1.0.sha256sum] = "8b870d8c151b6f2fb93e84a13146138f05d02ed11c7e7c54f8826aaaf7c9f184"
SRC_URI[ppv-lite86-0.2.16.sha256sum] = "eb9f9e6e233e5c4a35559a617bf40a4ec447db2e84c20b55a6f83167b7e57872"
SRC_URI[proc-macro-error-1.0.4.sha256sum] = "da25490ff9892aab3fcf7c36f08cfb902dd3e71ca0f9f9517bea02a73a5ce38c"
SRC_URI[proc-macro-error-attr-1.0.4.sha256sum] = "a1be40180e52ecc98ad80b184934baf3d0d29f979574e439af5a55274b35f869"
SRC_URI[proc-macro-hack-0.5.19.sha256sum] = "dbf0c48bc1d91375ae5c3cd81e3722dff1abcf81a30960240640d223f59fe0e5"
SRC_URI[proc-macro2-1.0.43.sha256sum] = "0a2ca2c61bc9f3d74d2886294ab7b9853abd9c1ad903a3ac7815c58989bb7bab"
SRC_URI[quote-1.0.21.sha256sum] = "bbe448f377a7d6961e30f5955f9b8d106c3f5e449d493ee1b125c1d43c2b5179"
SRC_URI[rand-0.8.5.sha256sum] = "34af8d1a0e25924bc5b7c43c079c942339d8f0a8b57c39049bef581b46327404"
SRC_URI[rand_chacha-0.3.1.sha256sum] = "e6c10a63a0fa32252be49d21e7709d4d4baf8d231c2dbce1eaa8141b9b127d88"
SRC_URI[rand_core-0.6.3.sha256sum] = "d34f1408f55294453790c48b2f1ebbb1c5b4b7563eb1f418bcfcfdbb06ebb4e7"
SRC_URI[regex-1.6.0.sha256sum] = "4c4eb3267174b8c6c2f654116623910a0fef09c4753f8dd83db29c48a0df988b"
SRC_URI[regex-syntax-0.6.27.sha256sum] = "a3f87b73ce11b1619a3c6332f45341e0047173771e8b8b73f87bfeefb7b56244"
SRC_URI[rustc-demangle-0.1.21.sha256sum] = "7ef03e0a2b150c7a90d01faf6254c9c48a41e95fb2a8c2ac1c6f0d2b9aefc342"
SRC_URI[ryu-1.0.11.sha256sum] = "4501abdff3ae82a1c1b477a17252eb69cee9e66eb915c1abaa4f44d873df9f09"
SRC_URI[serde-1.0.143.sha256sum] = "53e8e5d5b70924f74ff5c6d64d9a5acd91422117c60f48c4e07855238a254553"
SRC_URI[serde-generate-0.24.0.sha256sum] = "30f6496661c4b50af439d30e60c496c582aa98e55b7686a8d2c274b3f2a67d36"
SRC_URI[serde-reflection-0.3.6.sha256sum] = "f05a5f801ac62a51a49d378fdb3884480041b99aced450b28990673e8ff99895"
SRC_URI[serde_bytes-0.11.7.sha256sum] = "cfc50e8183eeeb6178dcb167ae34a8051d63535023ae38b5d8d12beae193d37b"
SRC_URI[serde_derive-1.0.143.sha256sum] = "d3d8e8de557aee63c26b85b947f5e59b690d0454c753f3adeb5cd7835ab88391"
SRC_URI[serde_yaml-0.8.26.sha256sum] = "578a7433b776b56a35785ed5ce9a7e777ac0598aac5a6dd1b4b18a307c7fc71b"
SRC_URI[siphasher-0.3.10.sha256sum] = "7bd3e3206899af3f8b12af284fafc038cc1dc2b41d1b89dd17297221c5d225de"
SRC_URI[slab-0.4.7.sha256sum] = "4614a76b2a8be0058caa9dbbaf66d988527d86d003c11a94fbd335d7661edcef"
SRC_URI[smawk-0.3.1.sha256sum] = "f67ad224767faa3c7d8b6d91985b78e70a1324408abcb1cfcc2be4c06bc06043"
SRC_URI[strsim-0.8.0.sha256sum] = "8ea5119cdb4c55b55d432abb513a0429384878c15dde60cc77b1c99de1a95a6a"
SRC_URI[structopt-0.3.26.sha256sum] = "0c6b5c64445ba8094a6ab0c3cd2ad323e07171012d9c98b0b15651daf1787a10"
SRC_URI[structopt-derive-0.4.18.sha256sum] = "dcb5ae327f9cc13b68763b5749770cb9e048a99bd9dfdfa58d0cf05d5f64afe0"
SRC_URI[syn-1.0.99.sha256sum] = "58dbef6ec655055e20b86b15a8cc6d439cca19b667537ac6a1369572d151ab13"
SRC_URI[synstructure-0.12.6.sha256sum] = "f36bdaa60a83aca3921b5259d5400cbf5e90fc51931376a9bd4a0eb79aa7210f"
SRC_URI[textwrap-0.11.0.sha256sum] = "d326610f408c7a4eb6f51c37c330e496b08506c9457c9d34287ecc38809fb060"
SRC_URI[textwrap-0.13.4.sha256sum] = "cd05616119e612a8041ef58f2b578906cc2531a6069047ae092cfb86a325d835"
SRC_URI[thiserror-1.0.32.sha256sum] = "f5f6586b7f764adc0231f4c79be7b920e766bb2f3e51b3661cdb263828f19994"
SRC_URI[thiserror-impl-1.0.32.sha256sum] = "12bafc5b54507e0149cdf1b145a5d80ab80a90bcd9275df43d4fff68460f6c21"
SRC_URI[tokio-1.20.1.sha256sum] = "7a8325f63a7d4774dd041e363b2409ed1c5cbbd0f867795e661df066b2b0a581"
SRC_URI[unicode-ident-1.0.3.sha256sum] = "c4f5b37a154999a8f3f98cc23a628d850e154479cd94decf3414696e12e31aaf"
SRC_URI[unicode-segmentation-1.9.0.sha256sum] = "7e8820f5d777f6224dc4be3632222971ac30164d4a258d595640799554ebfd99"
SRC_URI[unicode-width-0.1.9.sha256sum] = "3ed742d4ea2bd1176e236172c8429aaf54486e7ac098db29ffe6529e0ce50973"
SRC_URI[unicode-xid-0.2.3.sha256sum] = "957e51f3646910546462e67d5f7599b9e4fb8acdd304b087a6494730f9eebf04"
SRC_URI[vec_map-0.8.2.sha256sum] = "f1bddf1187be692e79c5ffeab891132dfb0f236ed36a43c7ed39f1165ee20191"
SRC_URI[version_check-0.9.4.sha256sum] = "49874b5167b65d7193b8aba1567f5c7d93d001cafc34600cee003eda787e483f"
SRC_URI[wasi-0.11.0+wasi-snapshot-preview1.sha256sum] = "9c8d87e72b64a3b4db28d11ce29237c246188f4f51057d65a7eab63b7987e423"
SRC_URI[winapi-0.3.9.sha256sum] = "5c839a674fcd7a98952e593242ea400abe93992746761e38641405d28b00f419"
SRC_URI[winapi-i686-pc-windows-gnu-0.4.0.sha256sum] = "ac3b87c63620426dd9b991e5ce0329eff545bccbbb34f3be09ff6fb6ab51b7b6"
SRC_URI[winapi-x86_64-pc-windows-gnu-0.4.0.sha256sum] = "712e227841d057c1ee1cd2fb22fa7e5a5461ae8e48fa2ca79ec42cfc1931183f"
SRC_URI[yaml-rust-0.4.5.sha256sum] = "56c1936c4cc7a1c9ab21a1ebb602eb942ba868cbd44a99cb7cdc5892335e1c85"

S = "${WORKDIR}/git"

CARGO_SRC_DIR = "rust_example"

RUNTIME:class-native = "llvm"
TOOLCHAIN:class-native = "clang"
PREFERRED_PROVIDER_libgcc:class-native = "compiler-rt"
LIBCPLUSPLUS:class-native = "-stdlib=libc++"

inherit cargo

#
# Dart generation
#
do_compile[network] = "1"
cargo_do_compile:class-native() {    

    export RUSTFLAGS
    export RUST_BACKTRACE=full
    export CARGO_TARGET_DIR=${S}/target
    export CARGO_BUILD_TARGET="${HOST_SYS}"

    export PUB_CACHE=${WORKDIR}/pub_cache
    export MEMBRANE_LLVM_PATHS=${STAGING_DIR_NATIVE}/usr
    export PATH="${STAGING_DIR_NATIVE}/usr/share/flutter/sdk/bin:$PATH"

    cd ${S}/${CARGO_SRC_DIR}

    # requires Dart
    bbnote `dart --version`

    # requires libclang.so
    bbnote `find ${STAGING_DIR_NATIVE} -iname libclang.so*`

    "${CARGO}" run -vv
}

cargo_do_install:class-native() {

    install -d ${D}${datadir}/membrane/dart_example

    cp -r ${S}/dart_example/* ${D}${datadir}/membrane/dart_example
}

cargo_do_install:append:class-target() {

    rm -rf ${D}${bindir}
}

# remove symbol stripping from Cargo build - Yocto does it
cargo_do_compile:prepend:class-target() {

    sed -i '/strip = \"symbols\"/d' ${S}/${CARGO_SRC_DIR}/Cargo.toml
}

FILES:${PN}:class-native = "${datadir}"

FILES:${PN} = "${libdir}"

BBCLASSEXTEND += "native"
