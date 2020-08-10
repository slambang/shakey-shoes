What is it?
-----------
A hardware/software project that vibrates a wireless device (shoes) in time to the music being listened to by the user.
The end result is that it feels like you're standing next to a giant, loud speaker. Why not?

Current State
-------------
The current state of the project is "prototype almost ready". It has required huge amounts of research, experimentation,
and trial & error. My first goal since the beginning was "just get something working". I'm almost there (estimate 4 weeks).

Components
----------
### Android

![](https://github.com/slambang/shakey_shoes/blob/master/resources/android_diagram.png)

[main_app](https://github.com/slambang/shakey_shoes/tree/master/android/main_app): __Working__  
This the main app for the project where all the work will eventually be collated. It connects to the Arduino, configures the remote buffer and begins synchronising the vibration data.

- Kotlin/C++/JNI using Android Studio
- Connects to Arduino via Bluetooth (see 'rcb' module)
- Calls [fingerband](https://github.com/slambang/shakey_shoes/tree/master/common/libfingerband/libfingerband) for audio analysis (currently used for for testing purposes. See [test_runner](https://github.com/slambang/shakey_shoes/tree/master/test/test_runner))

![Main App Connected](https://github.com/slambang/shakey_shoes/blob/master/resources/main_app_connected.gif)

[audio_test](https://github.com/slambang/shakey_shoes/tree/master/android/audio_test/samples/hello-oboe) : __WIP__ (heavily adapted 'hello-oboe' sample project)  
The project requires low latency audio, which is provided by Android's native [Oboe](https://github.com/google/oboe) library. I have a fully working example which has been adapted from an Oboe sample. It can open a Wav file and play the audio while also analysing the audio data, pushing the analysis results to the JVM via a direct buffer.

- Kotlin/C++/JNI using Android Studio
- Interfaces with [Oboe](https://github.com/google/oboe) to play wav audio natively
- Native circular buffer
- Refilled by native worker thread
- Mocks audio analysis
- Passes audio analysis results to JVM

### Arduino

![](https://github.com/slambang/shakey_shoes/blob/master/resources/arduino_diagram.png)

I started with an Arduino Duo, but quickly moved onto ESP8266 for size and capability purposes. I currently have 2 versions working (Mk0 and Mk1) both consisting of:

- [ESP8266 MCU](https://www.amazon.co.uk/IZOKEE-Internet-Development-Wireless-Compatible/dp/B01N4OYOKD/ref=sr_1_2?dchild=1&keywords=esp8266+nodemcu&qid=1596736642&s=computers&sr=1-2)
- [HC05 Bluetooth module](https://www.amazon.co.uk/DSD-TECH-HC-05-Pass-through-Communication/dp/B01G9KSAF6/ref=sr_1_1?dchild=1&keywords=hc05&qid=1596736701&s=computers&sr=1-1)
- [Vibrating motor](https://shop.pimoroni.com/products/vibrating-mini-motor-disc?variant=1038384249&currency=GBP&utm_source=google&utm_medium=cpc&utm_campaign=google+shopping&gclid=EAIaIQobChMIgO3h8pOH6wIVh7PtCh3huQKmEAQYAiABEgKKjfD_BwE)
- [LED](https://shop.pimoroni.com/products/led-3mm-pack-of-10?variant=32754744714)

![](https://github.com/slambang/shakey_shoes/blob/master/resources/Mk1.png)

[circular_buffer](https://github.com/slambang/shakey_shoes/tree/master/common/circular_buffer): __Working__  
The Arduino essentially acts a remote circular buffer, consuming items at regular intervals. This component implements the buffer abstractly with no Arduino dependencies. The buffer is refilled periodically from a connected client. Each item in the buffer is merely a byte value, which is delivered to a platform-specific listener to be used.

- C/C++ using CLion
- The abstract logic to be run on the Arduino
- Provides interfaces platform specific imlementation (Desktop, Arduino)
- The bulk of the Arduino App (but no Arduino dependencies)

[arduino_app](https://github.com/slambang/shakey_shoes/tree/master/arduino/arduino_app) : __Working__  
This is a very small wrapper around [circular_buffer](https://github.com/slambang/shakey_shoes/tree/master/common/circular_buffer), adapting it for the Arduino platform. When items are received from the buffer, this app uses the values to instruct the vibrating motor to activate with different intensities.

- C/C++ using Arduino IDE
- Runs on the ESP8266

[mock_app](https://github.com/slambang/shakey_shoes/tree/master/arduino/mock_app): __Working__  
This is also a wrapper around [circular_buffer](https://github.com/slambang/shakey_shoes/tree/master/common/circular_buffer) adapting it for the desktop platform. This app is used to provide debugging capabilities for [circular_buffer](https://github.com/slambang/shakey_shoes/tree/master/common/circular_buffer) (which are not available on the ESP8266).
- C/C++ using CLion
- Desktop platform implementations
- Runs the Arduino app on desktop

### Audio Analysis

![](https://github.com/slambang/shakey_shoes/blob/master/resources/fingerband_diagram.png)

[fingerband](https://github.com/slambang/shakey_shoes/tree/master/common/libfingerband/libfingerband) : __Working__  
This library analyses chunks of audio data to determine how much bass intensity is present. The result of a single analysis represents one buffer item to be consumed by the Arduino.

- C/C++ using CLion
- Audio analysis functionality
- Compiles on Android and Desktop (for easier research, testing, experimenting)
	- [fingerband/desktop_harness](https://github.com/slambang/shakey_shoes/tree/master/common/libfingerband/desktop_harness)
	- [android/main_app](https://github.com/slambang/shakey_shoes/tree/master/android/main_app)
- Optional preprocessor flag to output parsable debug logs

[debug_viewer](https://github.com/slambang/shakey_shoes/tree/master/test/debug_viewer): __Working__  
It is essential to visualise the algorithms and results used in [fingerband](https://github.com/slambang/shakey_shoes/tree/master/common/libfingerband/libfingerband). This project parses the debug logs and repesents them in the form of graphs. This allows me to see how the analysis algorithms are performing and debug issues.

- Python using PyCharm
- Parses debug logs from [fingerband](https://github.com/slambang/shakey_shoes/tree/master/common/libfingerband/libfingerband)
- Visualises audio analysis with graphs

![debug_viewer example](https://github.com/slambang/shakey_shoes/blob/hotfix/dsp_improvements/resources/debug_viewer_graph.gif)

[test_runner](https://github.com/slambang/shakey_shoes/tree/master/test/test_runner): __Working__  
In order to test [fingerband](https://github.com/slambang/shakey_shoes/tree/master/common/libfingerband/libfingerband) is working correctly on both Android (target platform) and Desktop (testing platform), I wrote this set of scripts. They invoke fingerband on both platforms, pull the debug logs into a central place and compares them for equality.

- Bash scripts
- Both sets of debug logs can be visualised with [debug_viewer](https://github.com/slambang/shakey_shoes/tree/master/test/debug_viewer)

Next Steps
----------
The next step is to combine the two Android apps (bringing the [audio_test](https://github.com/slambang/shakey_shoes/tree/master/android/audio_test/samples/hello-oboe) app into the [main_app](https://github.com/slambang/shakey_shoes/tree/master/android/main_app)). They each solve half the puzzle, so together I should have my very first v1 prototype, end-to-end fully working.

Then:
- Take what I ended up with and strip away anything that's redundant from all components
- Focus on each component; refactoring, tidying, adding tests
- Release
- Start improving to the audio analysis for better bass detection
- Start improving the synronisation between what the user hears and feels at the same time
