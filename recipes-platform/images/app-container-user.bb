SUMMARY = "Setup dev user"
DESCRIPTION = "Defines user for container usage that is not root (uid=0)"
SECTION = "devtools"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

SRC_URI = "file://dev_profile"

S = "${WORKDIR}"

EXCLUDE_FROM_WORLD = "1"

inherit useradd

# You must set USERADD_PACKAGES when you inherit useradd. This
# lists which output packages will include the user/group
# creation code.
USERADD_PACKAGES = "${PN}"

USERADD_PARAM:${PN} = "-u 5000 -d /home/dev -r -s /bin/bash dev"

GROUPADD_PARAM:${PN} = "-g 880 group1"

do_install () {

	install -D -p -m 0644 dev_profile ${D}/home/dev/.profile

	# The new users and groups are created before the do_install
	# step, so you are now free to make use of them:
	chown -R sim ${D}/home/dev/.profile
}

FILES:${PN} = "/home/dev/*"

# Prevents do_package failures with:
# debugsources.list: No such file or directory:
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
