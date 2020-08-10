This library analyses chunks of audio data to determine how much bass intensity is present. The result of a single analysis represents one buffer item to be consumed by the Arduino.

- C/C++ using CLion
- Audio analysis functionality
- Compiles on Android and Desktop (for easier research, testing, experimenting)
	- [fingerband/desktop_harness](https://github.com/slambang/shakey_shoes/tree/master/common/libfingerband/desktop_harness)
	- [android/main_app](https://github.com/slambang/shakey_shoes/tree/master/android/main_app)
- Parsable debug logs to visualise the algorithms (see [debug_viewer](https://github.com/slambang/shakey_shoes/tree/master/test/debug_viewer))
