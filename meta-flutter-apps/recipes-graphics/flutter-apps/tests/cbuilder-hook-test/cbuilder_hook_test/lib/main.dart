// SPDX-License-Identifier: MIT

import 'package:flutter/widgets.dart';

import 'c.dart';
import 'cpp.dart';

void main() {
  runApp(
    Directionality(
      textDirection: TextDirection.ltr,
      child: Center(child: Text('add(1, 2) = ${add(1, 2)}, join(10, 200) = ${join(10, 200)}')),
    ),
  );
}
