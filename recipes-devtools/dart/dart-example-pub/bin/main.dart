// Uses a pub package, so the build has to resolve one offline from the
// vendored cache rather than only compiling SDK code.
import 'package:path/path.dart' as p;

void main(List<String> args) {
  final joined = p.join('usr', 'lib', 'dart-example-pub');
  print('hello from a dart app with a pub dependency: $joined');
}
