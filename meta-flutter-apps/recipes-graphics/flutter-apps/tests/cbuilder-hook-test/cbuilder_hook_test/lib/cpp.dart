// SPDX-License-Identifier: MIT

import 'dart:ffi';

@Native<Int32 Function(Int32, Int32)>(symbol: 'cbuilder_hook_test_join')
external int join(int a, int b);
