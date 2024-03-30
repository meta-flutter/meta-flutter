#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "rust_lib"
HOMEPAGE = "https://github.com/fzyzcjy/flutter_rust_bridge"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=480e9b5af92d888295493a5cc7f2238e"

SRCREV = "79b0746e792692f03d0b95ade60f29da6cd789a1"

SRC_URI += " \
    git://github.com/fzyzcjy/flutter_rust_bridge.git;lfs=0;branch=master;protocol=https;destsuffix=git \
    crate://crates.io/addr2line/0.21.0 \
    crate://crates.io/adler/1.0.2 \
    crate://crates.io/aho-corasick/1.1.2 \
    crate://crates.io/allo-isolate/0.1.24 \
    crate://crates.io/android_log-sys/0.3.1 \
    crate://crates.io/android_logger/0.13.3 \
    crate://crates.io/anyhow/1.0.75 \
    crate://crates.io/atomic/0.5.3 \
    crate://crates.io/autocfg/1.1.0 \
    crate://crates.io/backtrace/0.3.69 \
    crate://crates.io/bit_field/0.10.2 \
    crate://crates.io/bitflags/1.3.2 \
    crate://crates.io/build-target/0.4.0 \
    crate://crates.io/bumpalo/3.14.0 \
    crate://crates.io/bytemuck/1.14.0 \
    crate://crates.io/byteorder/1.5.0 \
    crate://crates.io/cc/1.0.83 \
    crate://crates.io/cfg-if/1.0.0 \
    crate://crates.io/color_quant/1.1.0 \
    crate://crates.io/console_error_panic_hook/0.1.7 \
    crate://crates.io/crc32fast/1.3.2 \
    crate://crates.io/crossbeam/0.8.2 \
    crate://crates.io/crossbeam-channel/0.5.9 \
    crate://crates.io/crossbeam-deque/0.8.4 \
    crate://crates.io/crossbeam-epoch/0.9.16 \
    crate://crates.io/crossbeam-queue/0.3.9 \
    crate://crates.io/crossbeam-utils/0.8.17 \
    crate://crates.io/crunchy/0.2.2 \
    crate://crates.io/dart-sys-fork/4.1.1 \
    crate://crates.io/dashmap/4.0.2 \
    crate://crates.io/delegate-attr/0.3.0 \
    crate://crates.io/either/1.9.0 \
    crate://crates.io/env_logger/0.10.1 \
    crate://crates.io/exr/1.71.0 \
    crate://crates.io/fdeflate/0.3.1 \
    crate://crates.io/flate2/1.0.28 \
    crate://crates.io/flume/0.11.0 \
    crate://crates.io/futures/0.3.29 \
    crate://crates.io/futures-channel/0.3.29 \
    crate://crates.io/futures-core/0.3.29 \
    crate://crates.io/futures-executor/0.3.29 \
    crate://crates.io/futures-io/0.3.29 \
    crate://crates.io/futures-macro/0.3.29 \
    crate://crates.io/futures-sink/0.3.29 \
    crate://crates.io/futures-task/0.3.29 \
    crate://crates.io/futures-util/0.3.29 \
    crate://crates.io/gif/0.12.0 \
    crate://crates.io/gimli/0.28.1 \
    crate://crates.io/half/2.2.1 \
    crate://crates.io/hermit-abi/0.3.3 \
    crate://crates.io/image/0.24.7 \
    crate://crates.io/jpeg-decoder/0.3.0 \
    crate://crates.io/js-sys/0.3.68 \
    crate://crates.io/lazy_static/1.4.0 \
    crate://crates.io/lebe/0.5.2 \
    crate://crates.io/libc/0.2.150 \
    crate://crates.io/lock_api/0.4.11 \
    crate://crates.io/log/0.4.20 \
    crate://crates.io/memchr/2.6.4 \
    crate://crates.io/memoffset/0.9.0 \
    crate://crates.io/miniz_oxide/0.7.1 \
    crate://crates.io/num/0.4.1 \
    crate://crates.io/num-bigint/0.4.4 \
    crate://crates.io/num-complex/0.4.4 \
    crate://crates.io/num-integer/0.1.45 \
    crate://crates.io/num-iter/0.1.43 \
    crate://crates.io/num-rational/0.4.1 \
    crate://crates.io/num-traits/0.2.17 \
    crate://crates.io/num_cpus/1.16.0 \
    crate://crates.io/object/0.32.1 \
    crate://crates.io/once_cell/1.18.0 \
    crate://crates.io/oslog/0.1.0 \
    crate://crates.io/pin-project-lite/0.2.13 \
    crate://crates.io/pin-utils/0.1.0 \
    crate://crates.io/png/0.17.10 \
    crate://crates.io/proc-macro2/1.0.70 \
    crate://crates.io/qoi/0.4.1 \
    crate://crates.io/quote/1.0.33 \
    crate://crates.io/rayon/1.8.0 \
    crate://crates.io/rayon-core/1.12.0 \
    crate://crates.io/regex/1.10.2 \
    crate://crates.io/regex-automata/0.4.3 \
    crate://crates.io/regex-syntax/0.8.2 \
    crate://crates.io/rustc-demangle/0.1.23 \
    crate://crates.io/scopeguard/1.2.0 \
    crate://crates.io/simd-adler32/0.3.7 \
    crate://crates.io/slab/0.4.9 \
    crate://crates.io/smallvec/1.11.2 \
    crate://crates.io/spin/0.9.8 \
    crate://crates.io/syn/2.0.39 \
    crate://crates.io/threadpool/1.8.1 \
    crate://crates.io/tiff/0.9.0 \
    crate://crates.io/tokio/1.34.0 \
    crate://crates.io/unicode-ident/1.0.12 \
    crate://crates.io/wasm-bindgen/0.2.91 \
    crate://crates.io/wasm-bindgen-backend/0.2.91 \
    crate://crates.io/wasm-bindgen-futures/0.4.41 \
    crate://crates.io/wasm-bindgen-macro/0.2.91 \
    crate://crates.io/wasm-bindgen-macro-support/0.2.91 \
    crate://crates.io/wasm-bindgen-shared/0.2.91 \
    crate://crates.io/web-sys/0.3.66 \
    crate://crates.io/weezl/0.1.7 \
    crate://crates.io/zune-inflate/0.2.54 \
"

SRC_URI[addr2line-0.21.0.sha256sum] = "8a30b2e23b9e17a9f90641c7ab1549cd9b44f296d3ccbf309d2863cfe398a0cb"
SRC_URI[adler-1.0.2.sha256sum] = "f26201604c87b1e01bd3d98f8d5d9a8fcbb815e8cedb41ffccbeb4bf593a35fe"
SRC_URI[aho-corasick-1.1.2.sha256sum] = "b2969dcb958b36655471fc61f7e416fa76033bdd4bfed0678d8fee1e2d07a1f0"
SRC_URI[allo-isolate-0.1.24.sha256sum] = "f2f5a5fd28223e6f3cafb7d9cd685f51eafdd71d33ca1229f8316925d5957240"
SRC_URI[android_log-sys-0.3.1.sha256sum] = "5ecc8056bf6ab9892dcd53216c83d1597487d7dacac16c8df6b877d127df9937"
SRC_URI[android_logger-0.13.3.sha256sum] = "c494134f746c14dc653a35a4ea5aca24ac368529da5370ecf41fe0341c35772f"
SRC_URI[anyhow-1.0.75.sha256sum] = "a4668cab20f66d8d020e1fbc0ebe47217433c1b6c8f2040faf858554e394ace6"
SRC_URI[atomic-0.5.3.sha256sum] = "c59bdb34bc650a32731b31bd8f0829cc15d24a708ee31559e0bb34f2bc320cba"
SRC_URI[autocfg-1.1.0.sha256sum] = "d468802bab17cbc0cc575e9b053f41e72aa36bfa6b7f55e3529ffa43161b97fa"
SRC_URI[backtrace-0.3.69.sha256sum] = "2089b7e3f35b9dd2d0ed921ead4f6d318c27680d4a5bd167b3ee120edb105837"
SRC_URI[bit_field-0.10.2.sha256sum] = "dc827186963e592360843fb5ba4b973e145841266c1357f7180c43526f2e5b61"
SRC_URI[bitflags-1.3.2.sha256sum] = "bef38d45163c2f1dde094a7dfd33ccf595c92905c8f8f4fdc18d06fb1037718a"
SRC_URI[build-target-0.4.0.sha256sum] = "832133bbabbbaa9fbdba793456a2827627a7d2b8fb96032fa1e7666d7895832b"
SRC_URI[bumpalo-3.14.0.sha256sum] = "7f30e7476521f6f8af1a1c4c0b8cc94f0bee37d91763d0ca2665f299b6cd8aec"
SRC_URI[bytemuck-1.14.0.sha256sum] = "374d28ec25809ee0e23827c2ab573d729e293f281dfe393500e7ad618baa61c6"
SRC_URI[byteorder-1.5.0.sha256sum] = "1fd0f2584146f6f2ef48085050886acf353beff7305ebd1ae69500e27c67f64b"
SRC_URI[cc-1.0.83.sha256sum] = "f1174fb0b6ec23863f8b971027804a42614e347eafb0a95bf0b12cdae21fc4d0"
SRC_URI[cfg-if-1.0.0.sha256sum] = "baf1de4339761588bc0619e3cbc0120ee582ebb74b53b4efbf79117bd2da40fd"
SRC_URI[color_quant-1.1.0.sha256sum] = "3d7b894f5411737b7867f4827955924d7c254fc9f4d91a6aad6b097804b1018b"
SRC_URI[console_error_panic_hook-0.1.7.sha256sum] = "a06aeb73f470f66dcdbf7223caeebb85984942f22f1adb2a088cf9668146bbbc"
SRC_URI[crc32fast-1.3.2.sha256sum] = "b540bd8bc810d3885c6ea91e2018302f68baba2129ab3e88f32389ee9370880d"
SRC_URI[crossbeam-0.8.2.sha256sum] = "2801af0d36612ae591caa9568261fddce32ce6e08a7275ea334a06a4ad021a2c"
SRC_URI[crossbeam-channel-0.5.9.sha256sum] = "14c3242926edf34aec4ac3a77108ad4854bffaa2e4ddc1824124ce59231302d5"
SRC_URI[crossbeam-deque-0.8.4.sha256sum] = "fca89a0e215bab21874660c67903c5f143333cab1da83d041c7ded6053774751"
SRC_URI[crossbeam-epoch-0.9.16.sha256sum] = "2d2fe95351b870527a5d09bf563ed3c97c0cffb87cf1c78a591bf48bb218d9aa"
SRC_URI[crossbeam-queue-0.3.9.sha256sum] = "b9bcf5bdbfdd6030fb4a1c497b5d5fc5921aa2f60d359a17e249c0e6df3de153"
SRC_URI[crossbeam-utils-0.8.17.sha256sum] = "c06d96137f14f244c37f989d9fff8f95e6c18b918e71f36638f8c49112e4c78f"
SRC_URI[crunchy-0.2.2.sha256sum] = "7a81dae078cea95a014a339291cec439d2f232ebe854a9d672b796c6afafa9b7"
SRC_URI[dart-sys-fork-4.1.1.sha256sum] = "933dafff26172b719bb9695dd3715a1e7792f62dcdc8a5d4c740db7e0fedee8b"
SRC_URI[dashmap-4.0.2.sha256sum] = "e77a43b28d0668df09411cb0bc9a8c2adc40f9a048afe863e05fd43251e8e39c"
SRC_URI[delegate-attr-0.3.0.sha256sum] = "51aac4c99b2e6775164b412ea33ae8441b2fde2dbf05a20bc0052a63d08c475b"
SRC_URI[either-1.9.0.sha256sum] = "a26ae43d7bcc3b814de94796a5e736d4029efb0ee900c12e2d54c993ad1a1e07"
SRC_URI[env_logger-0.10.1.sha256sum] = "95b3f3e67048839cb0d0781f445682a35113da7121f7c949db0e2be96a4fbece"
SRC_URI[exr-1.71.0.sha256sum] = "832a761f35ab3e6664babfbdc6cef35a4860e816ec3916dcfd0882954e98a8a8"
SRC_URI[fdeflate-0.3.1.sha256sum] = "64d6dafc854908ff5da46ff3f8f473c6984119a2876a383a860246dd7841a868"
SRC_URI[flate2-1.0.28.sha256sum] = "46303f565772937ffe1d394a4fac6f411c6013172fadde9dcdb1e147a086940e"
SRC_URI[flume-0.11.0.sha256sum] = "55ac459de2512911e4b674ce33cf20befaba382d05b62b008afc1c8b57cbf181"
SRC_URI[futures-0.3.29.sha256sum] = "da0290714b38af9b4a7b094b8a37086d1b4e61f2df9122c3cad2577669145335"
SRC_URI[futures-channel-0.3.29.sha256sum] = "ff4dd66668b557604244583e3e1e1eada8c5c2e96a6d0d6653ede395b78bbacb"
SRC_URI[futures-core-0.3.29.sha256sum] = "eb1d22c66e66d9d72e1758f0bd7d4fd0bee04cad842ee34587d68c07e45d088c"
SRC_URI[futures-executor-0.3.29.sha256sum] = "0f4fb8693db0cf099eadcca0efe2a5a22e4550f98ed16aba6c48700da29597bc"
SRC_URI[futures-io-0.3.29.sha256sum] = "8bf34a163b5c4c52d0478a4d757da8fb65cabef42ba90515efee0f6f9fa45aaa"
SRC_URI[futures-macro-0.3.29.sha256sum] = "53b153fd91e4b0147f4aced87be237c98248656bb01050b96bf3ee89220a8ddb"
SRC_URI[futures-sink-0.3.29.sha256sum] = "e36d3378ee38c2a36ad710c5d30c2911d752cb941c00c72dbabfb786a7970817"
SRC_URI[futures-task-0.3.29.sha256sum] = "efd193069b0ddadc69c46389b740bbccdd97203899b48d09c5f7969591d6bae2"
SRC_URI[futures-util-0.3.29.sha256sum] = "a19526d624e703a3179b3d322efec918b6246ea0fa51d41124525f00f1cc8104"
SRC_URI[gif-0.12.0.sha256sum] = "80792593675e051cf94a4b111980da2ba60d4a83e43e0048c5693baab3977045"
SRC_URI[gimli-0.28.1.sha256sum] = "4271d37baee1b8c7e4b708028c57d816cf9d2434acb33a549475f78c181f6253"
SRC_URI[half-2.2.1.sha256sum] = "02b4af3693f1b705df946e9fe5631932443781d0aabb423b62fcd4d73f6d2fd0"
SRC_URI[hermit-abi-0.3.3.sha256sum] = "d77f7ec81a6d05a3abb01ab6eb7590f6083d08449fe5a1c8b1e620283546ccb7"
SRC_URI[image-0.24.7.sha256sum] = "6f3dfdbdd72063086ff443e297b61695500514b1e41095b6fb9a5ab48a70a711"
SRC_URI[jpeg-decoder-0.3.0.sha256sum] = "bc0000e42512c92e31c2252315bda326620a4e034105e900c98ec492fa077b3e"
SRC_URI[js-sys-0.3.68.sha256sum] = "406cda4b368d531c842222cf9d2600a9a4acce8d29423695379c6868a143a9ee"
SRC_URI[lazy_static-1.4.0.sha256sum] = "e2abad23fbc42b3700f2f279844dc832adb2b2eb069b2df918f455c4e18cc646"
SRC_URI[lebe-0.5.2.sha256sum] = "03087c2bad5e1034e8cace5926dec053fb3790248370865f5117a7d0213354c8"
SRC_URI[libc-0.2.150.sha256sum] = "89d92a4743f9a61002fae18374ed11e7973f530cb3a3255fb354818118b2203c"
SRC_URI[lock_api-0.4.11.sha256sum] = "3c168f8615b12bc01f9c17e2eb0cc07dcae1940121185446edc3744920e8ef45"
SRC_URI[log-0.4.20.sha256sum] = "b5e6163cb8c49088c2c36f57875e58ccd8c87c7427f7fbd50ea6710b2f3f2e8f"
SRC_URI[memchr-2.6.4.sha256sum] = "f665ee40bc4a3c5590afb1e9677db74a508659dfd71e126420da8274909a0167"
SRC_URI[memoffset-0.9.0.sha256sum] = "5a634b1c61a95585bd15607c6ab0c4e5b226e695ff2800ba0cdccddf208c406c"
SRC_URI[miniz_oxide-0.7.1.sha256sum] = "e7810e0be55b428ada41041c41f32c9f1a42817901b4ccf45fa3d4b6561e74c7"
SRC_URI[num-0.4.1.sha256sum] = "b05180d69e3da0e530ba2a1dae5110317e49e3b7f3d41be227dc5f92e49ee7af"
SRC_URI[num-bigint-0.4.4.sha256sum] = "608e7659b5c3d7cba262d894801b9ec9d00de989e8a82bd4bef91d08da45cdc0"
SRC_URI[num-complex-0.4.4.sha256sum] = "1ba157ca0885411de85d6ca030ba7e2a83a28636056c7c699b07c8b6f7383214"
SRC_URI[num-integer-0.1.45.sha256sum] = "225d3389fb3509a24c93f5c29eb6bde2586b98d9f016636dff58d7c6f7569cd9"
SRC_URI[num-iter-0.1.43.sha256sum] = "7d03e6c028c5dc5cac6e2dec0efda81fc887605bb3d884578bb6d6bf7514e252"
SRC_URI[num-rational-0.4.1.sha256sum] = "0638a1c9d0a3c0914158145bc76cff373a75a627e6ecbfb71cbe6f453a5a19b0"
SRC_URI[num-traits-0.2.17.sha256sum] = "39e3200413f237f41ab11ad6d161bc7239c84dcb631773ccd7de3dfe4b5c267c"
SRC_URI[num_cpus-1.16.0.sha256sum] = "4161fcb6d602d4d2081af7c3a45852d875a03dd337a6bfdd6e06407b61342a43"
SRC_URI[object-0.32.1.sha256sum] = "9cf5f9dd3933bd50a9e1f149ec995f39ae2c496d31fd772c1fd45ebc27e902b0"
SRC_URI[once_cell-1.18.0.sha256sum] = "dd8b5dd2ae5ed71462c540258bedcb51965123ad7e7ccf4b9a8cafaa4a63576d"
SRC_URI[oslog-0.1.0.sha256sum] = "8343ce955f18e7e68c0207dd0ea776ec453035685395ababd2ea651c569728b3"
SRC_URI[pin-project-lite-0.2.13.sha256sum] = "8afb450f006bf6385ca15ef45d71d2288452bc3683ce2e2cacc0d18e4be60b58"
SRC_URI[pin-utils-0.1.0.sha256sum] = "8b870d8c151b6f2fb93e84a13146138f05d02ed11c7e7c54f8826aaaf7c9f184"
SRC_URI[png-0.17.10.sha256sum] = "dd75bf2d8dd3702b9707cdbc56a5b9ef42cec752eb8b3bafc01234558442aa64"
SRC_URI[proc-macro2-1.0.70.sha256sum] = "39278fbbf5fb4f646ce651690877f89d1c5811a3d4acb27700c1cb3cdb78fd3b"
SRC_URI[qoi-0.4.1.sha256sum] = "7f6d64c71eb498fe9eae14ce4ec935c555749aef511cca85b5568910d6e48001"
SRC_URI[quote-1.0.33.sha256sum] = "5267fca4496028628a95160fc423a33e8b2e6af8a5302579e322e4b520293cae"
SRC_URI[rayon-1.8.0.sha256sum] = "9c27db03db7734835b3f53954b534c91069375ce6ccaa2e065441e07d9b6cdb1"
SRC_URI[rayon-core-1.12.0.sha256sum] = "5ce3fb6ad83f861aac485e76e1985cd109d9a3713802152be56c3b1f0e0658ed"
SRC_URI[regex-1.10.2.sha256sum] = "380b951a9c5e80ddfd6136919eef32310721aa4aacd4889a8d39124b026ab343"
SRC_URI[regex-automata-0.4.3.sha256sum] = "5f804c7828047e88b2d32e2d7fe5a105da8ee3264f01902f796c8e067dc2483f"
SRC_URI[regex-syntax-0.8.2.sha256sum] = "c08c74e62047bb2de4ff487b251e4a92e24f48745648451635cec7d591162d9f"
SRC_URI[rustc-demangle-0.1.23.sha256sum] = "d626bb9dae77e28219937af045c257c28bfd3f69333c512553507f5f9798cb76"
SRC_URI[scopeguard-1.2.0.sha256sum] = "94143f37725109f92c262ed2cf5e59bce7498c01bcc1502d7b9afe439a4e9f49"
SRC_URI[simd-adler32-0.3.7.sha256sum] = "d66dc143e6b11c1eddc06d5c423cfc97062865baf299914ab64caa38182078fe"
SRC_URI[slab-0.4.9.sha256sum] = "8f92a496fb766b417c996b9c5e57daf2f7ad3b0bebe1ccfca4856390e3d3bb67"
SRC_URI[smallvec-1.11.2.sha256sum] = "4dccd0940a2dcdf68d092b8cbab7dc0ad8fa938bf95787e1b916b0e3d0e8e970"
SRC_URI[spin-0.9.8.sha256sum] = "6980e8d7511241f8acf4aebddbb1ff938df5eebe98691418c4468d0b72a96a67"
SRC_URI[syn-2.0.39.sha256sum] = "23e78b90f2fcf45d3e842032ce32e3f2d1545ba6636271dcbf24fa306d87be7a"
SRC_URI[threadpool-1.8.1.sha256sum] = "d050e60b33d41c19108b32cea32164033a9013fe3b46cbd4457559bfbf77afaa"
SRC_URI[tiff-0.9.0.sha256sum] = "6d172b0f4d3fba17ba89811858b9d3d97f928aece846475bbda076ca46736211"
SRC_URI[tokio-1.34.0.sha256sum] = "d0c014766411e834f7af5b8f4cf46257aab4036ca95e9d2c144a10f59ad6f5b9"
SRC_URI[unicode-ident-1.0.12.sha256sum] = "3354b9ac3fae1ff6755cb6db53683adb661634f67557942dea4facebec0fee4b"
SRC_URI[wasm-bindgen-0.2.91.sha256sum] = "c1e124130aee3fb58c5bdd6b639a0509486b0338acaaae0c84a5124b0f588b7f"
SRC_URI[wasm-bindgen-backend-0.2.91.sha256sum] = "c9e7e1900c352b609c8488ad12639a311045f40a35491fb69ba8c12f758af70b"
SRC_URI[wasm-bindgen-futures-0.4.41.sha256sum] = "877b9c3f61ceea0e56331985743b13f3d25c406a7098d45180fb5f09bc19ed97"
SRC_URI[wasm-bindgen-macro-0.2.91.sha256sum] = "b30af9e2d358182b5c7449424f017eba305ed32a7010509ede96cdc4696c46ed"
SRC_URI[wasm-bindgen-macro-support-0.2.91.sha256sum] = "642f325be6301eb8107a83d12a8ac6c1e1c54345a7ef1a9261962dfefda09e66"
SRC_URI[wasm-bindgen-shared-0.2.91.sha256sum] = "4f186bd2dcf04330886ce82d6f33dd75a7bfcf69ecf5763b89fcde53b6ac9838"
SRC_URI[web-sys-0.3.66.sha256sum] = "50c24a44ec86bb68fbecd1b3efed7e85ea5621b39b35ef2766b66cd984f8010f"
SRC_URI[weezl-0.1.7.sha256sum] = "9193164d4de03a926d909d3bc7c30543cecb35400c02114792c2cae20d5e2dbb"
SRC_URI[zune-inflate-0.2.54.sha256sum] = "73ab332fe2f6680068f3582b16a24f90ad7096d5d39b974d1c0aff0125116f02"

S = "${WORKDIR}/git"

CARGO_SRC_DIR = "frb_example/gallery/rust"


inherit cargo

do_configure:prepend() {
    export RUSTFLAGS="-C ${DEBUG_FLAGS}"
}
