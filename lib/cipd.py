# Copyright (C) 2023 Joel Winarske
#
# SPDX-License-Identifier: MIT
#

"""
BitBake 'Fetch' cipd implementation

cipd fetcher supports the SRC_URI with format of:
SRC_URI = "cipd://some.package/somepath;OptionA=xxx;OptionB=xxx;..."

Supported SRC_URI options are:

- name
   The name of the SRC_URI instance

- version
   The CIPD version string

- destsuffix
    The location where CIPD gets extracted to.
"""

import os
import bb
import subprocess
from   bb.fetch2 import FetchMethod
from   bb.fetch2 import FetchError
from   bb.fetch2 import UnpackError
from   bb.fetch2 import logger
from   bb.fetch2 import runfetchcmd
from   bb.fetch2 import subprocess_setup

class CIPD(FetchMethod):
    """Class to fetch cipd packages using depot_tools 'cipd'"""
    def supports(self, ud, d):
        """
        Check to see if a given url can be fetched with cipd.
        """
        return ud.type in ['cipd']

    def recommends_checksum(self, urldata):
        return False

    def urldata_init(self, ud, d):
        ud.basename = "*"
        ud.destsuffix = "" if "destsuffix" not in ud.parm else ud.parm["destsuffix"]
        version = ud.parm["version"]
        ud.dlpath = os.path.join(d.getVar("DL_DIR"),"cipd")
        cipd_package = ud.url.split(";")[0].replace("cipd://", "")
        filename = cipd_package + "_" + version + ".zip"
        ud.localfile = filename.replace(":", "").replace("/", "_")

    def localpath(self, ud, d):
        """Return the local filename of a cip url"""
        return os.path.join(ud.dlpath, ud.localfile)

    def download(self, ud, d):
        """Fetch urls"""
        # If cipd package exists, skip download
        if os.access(ud.localpath, os.R_OK):
            return True

        uri = ud.url.split(";")[0]

        bb.utils.mkdirhier(ud.dlpath)
        os.chdir(ud.dlpath)

        ud.basecmd = "export PATH=\"%s:${PATH}\"; \
            export XDG_CONFIG_HOME=%s; \
            export DEPOT_TOOLS_UPDATE=0; \
            export CURL_CA_BUNDLE=%s; \
            cipd pkg-fetch %s -version %s -out %s" % (
            d.getVar("DEPOT_TOOLS"),
            d.getVar("DEPOT_TOOLS_XDG_CONFIG_HOME"),
            d.getVar('CURL_CA_BUNDLE'),
            ud.url.split(";")[0].replace("cipd://", ""),
            ud.parm["version"],
            ud.localpath)

        logger.debug2("Fetching %s using command '%s'" % (ud.url, ud.basecmd))
        runfetchcmd(ud.basecmd, d, False, workdir=None)

        if not os.path.exists(ud.localpath):
            raise FetchError("The fetch command returned success for url %s but %s doesn't exist?!" % (uri, ud.localpath), uri)

        if os.path.getsize(ud.localpath) == 0:
            os.remove(ud.localpath)
            raise FetchError("The fetch of %s resulted in a zero size file. Deleting and failing." % (uri), uri)

        return True

    def unpack(self, ud, workdir, d):
        
        dest_dir = os.path.join(workdir, ud.destsuffix)
        bb.utils.mkdirhier(dest_dir)

        cmd = 'unzip -o %s -d %s' % (ud.localpath, dest_dir)
        logger.debug2("Unpacking CIPD package %s to %s" % (ud.localpath, dest_dir))
        logger.debug2("%s" % (cmd))
        try:
            subprocess.check_output(cmd, preexec_fn=subprocess_setup, shell=True, cwd=dest_dir, 
                                    stderr=subprocess.STDOUT, universal_newlines=True)
        except subprocess.CalledProcessError as e:
            raise UnpackError("Unpack command %s failed with return value %s\n%s" % (cmd, e.returncode, e.stdout), ud.url)

    def clean(self, ud, d):
        logger.debug2("Cleaning CIPD package %s" % (ud.localpath))
        bb.utils.remove(ud.localpath, recurse=True)
