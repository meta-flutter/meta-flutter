#
# Copyright (c) 2020-2023 Joel Winarske. All rights reserved.
#

SUMMARY = "Setup dev user"
DESCRIPTION = "Defines user for container usage that is not root (uid=0)"
SECTION = "devtools"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "file://dev_profile"

S = "${WORKDIR}"

APP_CONTAINER_USER_NAME ??= "dev"
APP_CONTAINER_USER_ID ??= "5000"

EXCLUDE_FROM_WORLD = "1"

inherit useradd

# You must set USERADD_PACKAGES when you inherit useradd. This
# lists which output packages will include the user/group
# creation code.
USERADD_PACKAGES = "${PN}"

USERADD_PARAM:${PN} = "-c '' -p '' -u ${APP_CONTAINER_USER_ID} -m ${APP_CONTAINER_USER_NAME}"

do_install () {

	install -D -m 0664 ${APP_CONTAINER_USER_NAME}_profile ${D}/home/${APP_CONTAINER_USER_NAME}/.profile

	# The new users and groups are created before the do_install
	# step, so you are now free to make use of them:
	chown -R ${APP_CONTAINER_USER_NAME}:${APP_CONTAINER_USER_NAME} ${D}/home/${APP_CONTAINER_USER_NAME}
}

FILES:${PN} = "\
    /home/${APP_CONTAINER_USER_NAME} \
	"

# Prevents do_package failures with:
# debugsources.list: No such file or directory:
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
