# Tools


## How to auto roll meta-flutter

1. Fork https://github.com/meta-flutter/meta-flutter

Change `FLUTTER_SDK_TAG ??= "3.22.2"` in conf/include/flutter-version.inc to desired Flutter SDK version.

2. run auto-roll script
```
tools/roll_meta_flutter.py --version 3.22.2
```

3. Test

4. Create PR documenting what was tested and targets tested on


*Note: Only stable branch versions will be accepted*
