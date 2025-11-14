#
# Copyright (c) 2020-2025 Joel Winarske. All rights reserved.
#

SUMMARY = "live_object_detection_ssd_mobilenet"
DESCRIPTION = "A new Flutter project."
AUTHOR = "Amish Garg"
HOMEPAGE = "None"
BUGTRACKER = "None"
SECTION = "graphics"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

SRCREV = "a9a0d709e1acb138bfe8925ce86a05923db662cb"
SRC_URI = " \
    git://github.com/meta-flutter/flutter-tflite.git;lfs=0;branch=toyotaflutter;protocol=https \
    https://storage.googleapis.com/download.tensorflow.org/models/tflite/task_library/object_detection/android/lite-model_ssd_mobilenet_v1_1_metadata_2.tflite;name=model_file;subdir=example/live_object_detection_ssd_mobilenet/assets/models \
"

S = "${WORKDIR}/git"

PUB_CACHE_EXTRA_ARCHIVE_PATH = "${WORKDIR}/pub_cache/bin"
PUB_CACHE_EXTRA_ARCHIVE_CMD = "flutter pub global activate melos; \
    melos bootstrap"

PUBSPEC_APPNAME = "live_object_detection_ssd_mobilenet"
FLUTTER_APPLICATION_INSTALL_SUFFIX = "meta-flutter-tflite-example-live-object-detection-ssd-mobilenet"
PUBSPEC_IGNORE_LOCKFILE = "1"
FLUTTER_APPLICATION_PATH = "example/live_object_detection_ssd_mobilenet"

SRC_URI[model_file.sha256sum] = "cbdecd08b44c5dea3821f77c5468e2936ecfbf43cde0795a2729fdb43401e58b"

inherit flutter-app

RDEPENDS:${PN} += " \
    tensorflow-lite \
"
