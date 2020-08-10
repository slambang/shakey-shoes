The project requires low latency audio, which is provided by Android's native [Oboe](https://github.com/google/oboe) library. I have a fully working example which has been adapted from an Oboe sample.

- Kotlin/C++/JNI using Android Studio
- Interfaces with [Oboe](https://github.com/google/oboe) to play wav audio natively at low latency
- Native circular buffers (audio and audio analysis results)
- Refilled by native worker thread (which also passes the audio analysis results to JVM)
- Will eventually be merged with [main_app](https://github.com/slambang/shakey_shoes/tree/master/android/main_app)
