SUMMARY = "Wayland compositor info tool using Wayland client toolkit."
HOMEPAGE = "https://github.com/smithay/client-toolkit"
LICENSE = "MIT"

LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=130a3a1db6a4a5113c4f76e3cf0a7acd"

REQUIRED_DISTRO_FEATURES = "wayland"

DEPENDS += "wayland wayland-native"

SRC_URI = "git://github.com/Smithay/client-toolkit.git;protocol=https;nobranch=1 \
           file://0001-release-profile.patch"
SRCREV = "3026a83f51980a88cb7e0270003b7079f429a6bb"
S = "${WORKDIR}/git"

inherit cargo features_check

CARGO_SRC_DIR = ""

CARGO_BUILD_FLAGS += "--example compositor_info"
CARGO_BUILD_FLAGS += "--example image_viewer"
CARGO_BUILD_FLAGS += "--example kbd_input"
CARGO_BUILD_FLAGS += "--example layer_shell"
CARGO_BUILD_FLAGS += "--example pointer_input"
CARGO_BUILD_FLAGS += "--example selection"
CARGO_BUILD_FLAGS += "--example themed_frame"

SRC_URI += " \
    crate://crates.io/ab_glyph_rasterizer/0.1.4 \
    crate://crates.io/adler/0.2.3 \
    crate://crates.io/adler32/1.2.0 \
    crate://crates.io/andrew/0.3.1 \
    crate://crates.io/autocfg/1.0.1 \
    crate://crates.io/bitflags/1.2.1 \
    crate://crates.io/bytemuck/1.5.1 \
    crate://crates.io/byteorder/1.4.2 \
    crate://crates.io/calloop/0.6.5 \
    crate://crates.io/cc/1.0.67 \
    crate://crates.io/cfg-if/0.1.10 \
    crate://crates.io/cfg-if/1.0.0 \
    crate://crates.io/color_quant/1.1.0 \
    crate://crates.io/crc32fast/1.2.1 \
    crate://crates.io/crossbeam-channel/0.5.0 \
    crate://crates.io/crossbeam-deque/0.8.0 \
    crate://crates.io/crossbeam-epoch/0.9.2 \
    crate://crates.io/crossbeam-utils/0.8.2 \
    crate://crates.io/deflate/0.8.6 \
    crate://crates.io/dlib/0.4.2 \
    crate://crates.io/downcast-rs/1.2.0 \
    crate://crates.io/either/1.6.1 \
    crate://crates.io/generator/0.6.24 \
    crate://crates.io/gif/0.11.1 \
    crate://crates.io/hermit-abi/0.1.18 \
    crate://crates.io/image/0.23.13 \
    crate://crates.io/jpeg-decoder/0.1.22 \
    crate://crates.io/lazy_static/1.4.0 \
    crate://crates.io/libc/0.2.86 \
    crate://crates.io/libloading/0.6.7 \
    crate://crates.io/log/0.4.14 \
    crate://crates.io/loom/0.4.0 \
    crate://crates.io/memchr/2.3.4 \
    crate://crates.io/memmap2/0.2.1 \
    crate://crates.io/memoffset/0.6.1 \
    crate://crates.io/miniz_oxide/0.3.7 \
    crate://crates.io/miniz_oxide/0.4.3 \
    crate://crates.io/nix/0.18.0 \
    crate://crates.io/nix/0.19.1 \
    crate://crates.io/nix/0.20.0 \
    crate://crates.io/nom/6.1.2 \
    crate://crates.io/num-integer/0.1.44 \
    crate://crates.io/num-iter/0.1.42 \
    crate://crates.io/num-rational/0.3.2 \
    crate://crates.io/num-traits/0.2.14 \
    crate://crates.io/num_cpus/1.13.0 \
    crate://crates.io/once_cell/1.6.0 \
    crate://crates.io/owned_ttf_parser/0.6.0 \
    crate://crates.io/pkg-config/0.3.19 \
    crate://crates.io/png/0.16.8 \
    crate://crates.io/proc-macro2/1.0.24 \
    crate://crates.io/quote/1.0.9 \
    crate://crates.io/rayon-core/1.9.0 \
    crate://crates.io/rayon/1.5.0 \
    crate://crates.io/rusttype/0.9.2 \
    crate://crates.io/rustversion/1.0.4 \
    crate://crates.io/same-file/1.0.6 \
    crate://crates.io/scoped-tls/1.0.0 \
    crate://crates.io/scoped_threadpool/0.1.9 \
    crate://crates.io/scopeguard/1.1.0 \
    crate://crates.io/smallvec/1.6.1 \
    crate://crates.io/tiff/0.6.1 \
    crate://crates.io/ttf-parser/0.6.2 \
    crate://crates.io/unicode-xid/0.2.1 \
    crate://crates.io/version_check/0.9.2 \
    crate://crates.io/walkdir/2.3.1 \
    crate://crates.io/wayland-client/0.28.4 \
    crate://crates.io/wayland-commons/0.28.4 \
    crate://crates.io/wayland-cursor/0.28.4 \
    crate://crates.io/wayland-protocols/0.28.4 \
    crate://crates.io/wayland-scanner/0.28.4 \
    crate://crates.io/wayland-sys/0.28.4 \
    crate://crates.io/weezl/0.1.4 \
    crate://crates.io/winapi-i686-pc-windows-gnu/0.4.0 \
    crate://crates.io/winapi-util/0.1.5 \
    crate://crates.io/winapi-x86_64-pc-windows-gnu/0.4.0 \
    crate://crates.io/winapi/0.3.9 \
    crate://crates.io/xcursor/0.3.3 \
    crate://crates.io/xdg/2.2.0 \
    crate://crates.io/xml-rs/0.8.3 \
"

cargo_do_install() {

	local have_installed=false
	for tgt in "${B}/target/${CARGO_TARGET_SUBDIR}/"*; do
        bbnote "$tgt"
		case $tgt in
		*.so|*.rlib)
			install -d "${D}${rustlibdir}"
			install -m755 "$tgt" "${D}${rustlibdir}"
			have_installed=true
			;;
        *examples)
            if [ -d "$tgt" ]; then
                for example in "$tgt/"*; do
                    if [ -f "$example" ] && [ -x "$example" ]; then
                        install -d "${D}${bindir}"
                        install -m755 "$example" "${D}${bindir}"
				        have_installed=true
                    fi
                done
            fi
            ;;
		*)
			if [ -f "$tgt" ] && [ -x "$tgt" ]; then
				install -d "${D}${bindir}"
				install -m755 "$tgt" "${D}${bindir}"
				have_installed=true
			fi
			;;
		esac
	done
	if ! $have_installed; then
		die "Did not find anything to install"
	fi
}

BBCLASSEXTEND = ""
