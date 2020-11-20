SUMMARY = "Flutter - Dart based UI framework SDK"
WEBSITE = "https://flutter.dev/"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=343c5a032261c8c32d621412cdcd70a8"

PV = "1.12.13"
SRC_URI = "https://storage.googleapis.com/flutter_infra/releases/stable/linux/flutter_linux_v${PV}+hotfix.9-stable.tar.xz"
SRC_URI[md5sum] = "52f21a77670556a4db5f668dac3adb3b"

S = "${WORKDIR}/flutter"

do_install() {

    install -d ${D}${datadir}/flutter/sdk
    cp -rTv ${S}/. ${D}${datadir}/flutter/sdk
}

FILES_${PN}-dev = "${datadir}/flutter/sdk/*"

INSANE_SKIP_${PN} = "already-stripped"

BBCLASSEXTEND =+ " native nativesdk"
