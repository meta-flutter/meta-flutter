"""
BitBake 'Fetch' implementations

This fetcher is created for cipd.
The main target is flutter-engine, so for other cipd projects, this fetcher might not work.

Copyright (c) 2021-2022 Woven Alpha, Inc
"""

import os
import bb
import multiprocessing
import subprocess
import urllib
from   bb.fetch2 import FetchMethod
from   bb.fetch2 import FetchError
from   bb.fetch2 import UnpackError
from   bb.fetch2 import logger
from   bb.fetch2 import runfetchcmd
from   bb.fetch2 import subprocess_setup

class CIPD(FetchMethod):
    def supports(self, ud, d):
        """
        Check to see if a given url can be fetched with gn.
        """
        return ud.type in ['cipd']

    def recommends_checksum(self, urldata):
        return False

    def urldata_init(self, ud, d):
        # syntax: cipd://<URL>;name=<NAME>;destsuffix=<D>
        name = ud.parm.get("name", "src")
        ud.destsuffix = "src" if "destsuffix" not in ud.parm else ud.parm["destsuffix"]

        ud.basename = "*"

        depot_tools_path = d.getVar("DEPOT_TOOLS")
        python2_path = d.getVar("PYTHON2_PATH")

        uri = ud.url.split(";")[0].replace("cipd://", "")

        logger.debug(2, "name=%s destsuffix=%s uri=%s" % (name, ud.destsuffix, uri))

        ud.setup_revisions(d)

        if name in ud.names:
            if name in ud.revisions:
                srcrev = d.getVar("SRCREV_%s" % (name))
            else:
                srcrev = d.getVar("SRCREV")
        else:
            srcrev = d.getVar("SRCREV")

        dl_dir = d.getVar("DL_DIR")
        ud.cipddir = os.path.join(dl_dir, "cipd")
        ud.cipdname = uri.replace(":", "").replace("/", "_") + "-" + srcrev
        ud.cipdpath = os.path.join(ud.cipddir, ud.cipdname)
        ud.localfile = ud.cipdname + ".zip"
        ud.localpath = os.path.join(ud.cipddir, ud.localfile)

        bb_number_threads=d.getVar("BB_NUMBER_THREADS", multiprocessing.cpu_count())
        cipd_max_threads=bb_number_threads
        cipd_parallel_downloads=bb_number_threads
        extra_cipd = d.getVar("EXTRA_CIPD", "")

        ud.basecmd = "export PATH=\"%s:%s:${PATH}\"; export DEPOT_TOOLS_UPDATE=0; export GCLIENT_PY3=0; \
            export CURL_CA_BUNDLE=%s; \
            export CIPD_CACHE_DIR=%s; \
            export CIPD_MAX_THREADS=%s; \
            export CIPD_PARALLEL_DOWNLOADS=%s; \
            cipd pkg-fetch %s %s -version %s -verbose -out %s" % (
                depot_tools_path, os.path.join(depot_tools_path, python2_path),
                d.getVar('CURL_CA_BUNDLE'),
                ud.cipdpath,
                cipd_max_threads,
                cipd_parallel_downloads,
                uri, extra_cipd, srcrev, ud.localpath)

    def _runcipd(self, ud, d, quiet):
        bb.utils.mkdirhier(ud.cipddir)
        bb.utils.mkdirhier(ud.cipdpath)
        os.chdir(ud.cipddir)

        logger.debug(2, "Fetching %s using command '%s'" % (ud.url, ud.basecmd))
        bb.fetch2.check_network_access(d, ud.basecmd, ud.url)
        runfetchcmd(ud.basecmd, d, quiet, workdir=None)

    def localpath(self, ud, d):
        """
        Return the local filename of a given url assuming a successful fetch.
        Can also setup variables in urldata for use in go (saving code duplication
        and duplicate code execution)
        """
        return ud.localpath

    def download(self, ud, d):
        """Fetch urls"""
        # If zip exists, skip
        if os.access(ud.localpath, os.R_OK):
            return True

        uri = ud.url.split(";")[0]

        self._runcipd(ud, d, False)

        # Sanity check since wget can pretend it succeed when it didn't
        # Also, this used to happen if sourceforge sent us to the mirror page
        if not os.path.exists(ud.localpath):
            raise FetchError("The fetch command returned success for url %s but %s doesn't exist?!" % (uri, ud.localpath), uri)

        if os.path.getsize(ud.localpath) == 0:
            os.remove(ud.localpath)
            raise FetchError("The fetch of %s resulted in a zero size file?! Deleting and failing since this isn't right." % (uri), uri)

        return True

    def unpack(self, ud, workdir, d):
        if not os.access(ud.localpath, os.R_OK):
            raise UnpackError("Could not access %s" % (ud.localpath))

        destdir = os.path.join(workdir, ud.destsuffix)
        bb.utils.mkdirhier(destdir)
        unpackcmd = "unzip %s -d %s" % (ud.localpath, destdir)
        runfetchcmd(unpackcmd, d, workdir=workdir)

    def clean(self, ud, d):
        bb.utils.remove(ud.localpath, recurse=True)
        bb.utils.remove(ud.cipdpath, recurse=True)

    def checkstatus(self, fetch, ud, d, try_again=True):
        return True

    def latest_versionstring(self, ud, d):
        """
        Manipulate the URL and try to obtain the latest package version

        sanity check to ensure same name and type.
        """
        return ("", '')

