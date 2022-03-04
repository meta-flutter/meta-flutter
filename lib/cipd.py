"""
 BitBake 'Fetch' implementations

 This fetcher is created for cipd.
 The main target is flutter-engine, so for other cipd projects, this fetcher might not work.

 Copyright (c) 2021-2022 Woven Alpha, Inc
 Copyright (c) 2022 Joel Winarske. All rights reserved.
 """

import os
import bb
import multiprocessing
from   bb.fetch2 import FetchError
from   bb.fetch2 import FetchMethod
from   bb.fetch2 import logger
from   bb.fetch2 import runfetchcmd
from   bb.fetch2 import UnpackError

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
        uri = ud.url.split(";")[0].replace("cipd://", "")

        name = ud.parm.get("name", "")
        ud.destsuffix = "" if "destsuffix" not in ud.parm else ud.parm["destsuffix"]
        ud.basename = "*"

        ud.setup_revisions(d)

        if name in ud.names:
            if name in ud.revisions:
                srcrev = d.getVar("SRCREV_%s" % (name))
            else:
                srcrev = d.getVar("SRCREV")
        else:
            srcrev = d.getVar("SRCREV")

        cipd_cache_path = os.path.join(d.getVar('WORKDIR'), 'cipd')

        ca_bundle_path = d.getVar('CURL_CA_BUNDLE')

        bb_number_threads=d.getVar("BB_NUMBER_THREADS", multiprocessing.cpu_count())
        cipd_max_threads = bb_number_threads
        cipd_parallel_downloads = bb_number_threads

        ud.localfile = uri.replace(":", "").replace("/", "_") + "-" + srcrev + ".zip"        
        ud.localpath = os.path.join(d.getVar("DL_DIR"), ud.localfile)

        extra_cipd_parameters = d.getVar("EXTRA_CIPD", "")

        ud.basecmd = " \
            CURL_CA_BUNDLE=%s CIPD_CACHE_DIR=%s CIPD_MAX_THREADS=%s CIPD_PARALLEL_DOWNLOADS=%s \
            %s pkg-fetch %s %s -version %s -verbose -out %s" % (
                ca_bundle_path,
                cipd_cache_path,
                cipd_max_threads,
                cipd_parallel_downloads,
                d.getVar("FETCHCMD_cipd") or "/usr/bin/env cipd",
                uri, extra_cipd_parameters,
                srcrev,
                ud.localpath
                )
        
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

        logger.debug(2, "Fetching %s using command '%s'" % (ud.url, ud.basecmd))
        runfetchcmd(ud.basecmd, d, False, workdir=None)

        # Sanity check since wget can pretend it succeed when it didn't
        # Also, this used to happen if sourceforge sent us to the mirror page
        if not os.path.exists(ud.localpath):
            raise FetchError("The fetch command returned success for url %s but %s doesn't exist?!" % (uri, ud.localpath), uri)

        if os.path.getsize(ud.localpath) == 0:
            os.remove(ud.localpath)
            raise FetchError("The fetch of %s resulted in a zero size file?! Deleting and failing since this isn't right." % (uri), uri)

        return True

    def unpack(self, ud, root, d):
        if not os.access(ud.localpath, os.R_OK):
            raise UnpackError("Could not access %s" % (ud.localpath))

        destdir = os.path.join(root, ud.destsuffix)
        bb.utils.mkdirhier(destdir)
        logger.debug(2, "extracting %s to: %s" % (ud.localpath, destdir))
        unpackcmd = "unzip %s -d %s" % (ud.localpath, destdir)
        runfetchcmd(unpackcmd, d, workdir=root)

    def clean(self, ud, d):
        bb.utils.remove(ud.localpath, recurse=True)

    def checkstatus(self, fetch, ud, d, try_again=True):
        return True

    def latest_versionstring(self, ud, d):
        """
        Manipulate the URL and try to obtain the latest package version

        sanity check to ensure same name and type.
        """
        return ("", '')
