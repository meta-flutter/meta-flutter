#
# SPDX-FileCopyrightText: (c) 2026 Joel Winarske
#
# SPDX-License-Identifier: MIT
#
# meta-oe builds capnproto static and non-PIC, so linking capnp or kj into a
# shared library fails on kj/exception.c++'s thread-local:
#
#   libkj.a(exception.c++.o): relocation R_X86_64_TPOFF32 against
#   `kj::(anonymous namespace)::threadLocalCallback' can not be used when making
#   a shared object; local-exec is incompatible with -shared
#
# test-runner hits this: libTestRunnerClient is SHARED and links both. Code model
# only -- still static, no .so, no new packages -- but capnproto's sstate is
# invalidated. Belongs in meta-oe; drop this when it lands there.
#
EXTRA_OECMAKE:append = " -DCMAKE_POSITION_INDEPENDENT_CODE=ON"
