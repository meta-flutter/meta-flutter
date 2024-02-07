#
# Copyright (c) 2020-2024 Joel Winarske. All rights reserved.
#

SUMMARY = "printing_demo"
DESCRIPTION = "Pdf Printing Demo"
AUTHOR = "David PHAM-VAN"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5335066555b14d832335aa4660d6c376"

SRCREV = "19d9f4bf6e5dc69e53799681c062970f62b3eef9"
SRC_URI = "\
    git://github.com/DavBfr/dart_pdf.git;lfs=0;branch=master;protocol=https;destsuffix=git \
    http://pigment.github.io/fake-logos/logos/vector/color/auto-speed.svg;subdir=demo/assets/;downloadfilename=logo.svg;name=logo \
    https://www.fakepersongenerator.com/Face/female/female20151024334209870.jpg;subdir=demo/assets/;downloadfilename=profile.jpg;name=profile \
    https://fonts.gstatic.com/s/opensans/v17/mem8YaGs126MiZpBA-U1Ug.ttf;subdir=pdf/;downloadfilename=open-sans.ttf;name=open_sans \
    https://fonts.gstatic.com/s/opensans/v17/mem5YaGs126MiZpBA-UN7rg-VQ.ttf;subdir=pdf/;downloadfilename=open-sans-bold.ttf;name=open_sans_bold \
    https://fonts.gstatic.com/s/robotomono/v7/L0x5DF4xlVMF-BfR8bXMIghM.ttf;subdir=pdf/;downloadfilename=roboto.ttf;name=roboto \
    https://fonts.gstatic.com/s/notosans/v9/o-0IIpQlx3QUlC5A4PNb4g.ttf;subdir=pdf/;downloadfilename=noto-sans.ttf;name=noto_sans \
    https://github.com/ButTaiwan/genyo-font/raw/bc2fa246196fefc1ef9e9843bc8cdba22523a39d/TW/GenYoMinTW-Heavy.ttf;subdir=pdf/;downloadfilename=genyomintw.ttf;name=genyomintw \
    http://www.aboaziz.net/misc/arabic%20fonts%20pack/%CE%D8%E6%D8%20%DA%D1%C8%ED%C9%20%CD%CF%ED%CB%C9%20%E6%E3%E3%ED%D2%C9/%CE%D8%E6%D8%20%DA%D1%C8%ED%C9%20%CD%CF%ED%CB%C9%20%E6%20%E3%E3%ED%D2%C9/Hacen%20Tunisia/Hacen%20Tunisia.ttf;subdir=pdf/;downloadfilename=hacen-tunisia.ttf;name=hacen_tunisia \
    https://github.com/google/material-design-icons/raw/master/font/MaterialIcons-Regular.ttf;subdir=pdf/;downloadfilename=material.ttf;name=material \
    https://github.com/googlefonts/noto-emoji/raw/main/fonts/NotoColorEmoji.ttf;subdir=pdf/;downloadfilename=pdf/emoji.ttf;name=emoji \
"
SRC_URI[logo.sha256sum] = "9717756f813692f1ac2c5b1d3d813f2b239075355379223bfb45618c664d0f28"
SRC_URI[profile.sha256sum] = "40638e9fad1e2b372420651a8ac41ffa7864e87672a884a85cea1d62514a4982"
SRC_URI[open_sans.sha256sum] = "b31b29a36863fed7e0d370f54e142ef7028b72915293aac0c441c0599f4cab13"
SRC_URI[open_sans_bold.sha256sum] = "e29dc8ef81abb32e95492f2003fcd6b226d666c1496c6d07a8a66c3f37258826"
SRC_URI[roboto.sha256sum] = "cc79904e8453f0e632d76dc86d47f0dfb412559105454c7065f8951ea94b2daf"
SRC_URI[noto_sans.sha256sum] = "f95fc61d04cc4c373fa58adb0e1ccd94f3607336d5e50f16363c6d823c84bba0"
SRC_URI[genyomintw.sha256sum] = "9a1cbf26d158da99652ad295c5300526b7ae84fef799242fecc3c4d43214d477"
SRC_URI[hacen_tunisia.sha256sum] = "c0e9e40cd409c8d9470caea8de6e3bf20a1139c5d9f67d779c0b067324422fa7"
SRC_URI[material.sha256sum] = "ef149f08bdd2ff09a4e2c8573476b7b0f3fbb15b623954ade59899e7175bedda"
SRC_URI[emoji.sha256sum] = "c2f19f6a404baa7da7a710b018c2892d7b51386983ddca146811f76aea0b6861"

S = "${WORKDIR}/git"

PUBSPEC_APPNAME = "printing_demo"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "davbfr-dart-pdf-demo"
FLUTTER_APPLICATION_PATH = "demo"

inherit flutter-app
