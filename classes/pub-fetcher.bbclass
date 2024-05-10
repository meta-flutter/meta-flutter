# Copyright (C) 2024 Seungkyun Kim. All rights reserved.
#
# SPDX-License-Identifier: MIT
#

python () {
    import pub
    bb.fetch2.methods.append(pub.Pub())

    src_uri = (d.getVar('SRC_URI') or "").split()
    if not src_uri:
        return

    pub_entry_found = False
    for uri in src_uri:
        if uri.startswith('pub://'):
            pub_entry_found = True
            break

    if pub_entry_found:
        bb.debug(1, "Conditionally skipping do_archive_pub_cache do_restore_pub_cache")
        # Mark the task as "noexec" to skip its execution
        d.setVarFlag('do_archive_pub_cache', 'noexec', '1')
        d.setVarFlag('do_restore_pub_cache', 'noexec', '1')
}

