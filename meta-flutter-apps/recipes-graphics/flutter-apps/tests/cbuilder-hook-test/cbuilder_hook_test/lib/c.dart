// SPDX-License-Identifier: MIT

import 'dart:ffi';

@Native<Int32 Function(Int32, Int32)>(symbol: 'cbuilder_hook_test_add')
external int add(int a, int b);
