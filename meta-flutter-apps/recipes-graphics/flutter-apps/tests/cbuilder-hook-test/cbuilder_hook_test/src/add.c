// SPDX-License-Identifier: MIT

#include <stdint.h>

int cbuilder_hook_test_twice(int value);

// a + 2 * b, the second half from the static archive.
int32_t cbuilder_hook_test_add(int32_t a, int32_t b) {
  return a + cbuilder_hook_test_twice(b);
}
