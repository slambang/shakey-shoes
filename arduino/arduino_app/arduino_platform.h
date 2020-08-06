#include "circular_buffer/platform.h"

class ArdPlatform : public Platform {
public:
  // Hack: Ensure we have room to create the App instance before buffers are created
  static const int RESERVED_SPACE_SIZE_BYTES = 128;

  ArdPlatform(int motorPin, int statusLedPin, int bluetoothStatePin)
    : motorPin(motorPin), statusLedPin(statusLedPin), bluetoothStatePin(bluetoothStatePin) {
    initialise();
  }

  int freeHeapBytes() override {
    return ESP.getFreeHeap() - RESERVED_SPACE_SIZE_BYTES;
  }

  TIME timeNowMillis() override {
    return millis();
  }

  void onIntervalEvent(int value) override {
    analogWrite(motorPin, value);
  }

  void setLedEnabled(bool enabled) override {
    digitalWrite(statusLedPin, enabled == true ? HIGH : LOW);
  }

  bool isConnected() override {
    return digitalRead(bluetoothStatePin) == HIGH;
  }

private:
  void initialise() {
    analogWrite(motorPin, 0);
    pinMode(statusLedPin, OUTPUT);
    pinMode(bluetoothStatePin, INPUT);
    digitalWrite(bluetoothStatePin, LOW);

#if defined(ESP8266)
    analogWriteRange(255);
#endif
  }

  int motorPin;
  int statusLedPin;
  int bluetoothStatePin;
};
