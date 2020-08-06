#include "circular_buffer/appfactory.h"
#include "arduino_platform.h"
#include "arduino_data_stream.h"

/**
 * Link circular_buffer dependency
 * --------------------------------
 * Symlink the CircularBuffer library from this file's directory:
 *      `ln -s ../../common/circular_buffer circular_buffer`
 *
 * Add Node MCU ESP8266
 * --------------------
 *      - File > Preferences > Additional Boards Manager URLS: http://arduino.esp8266.com/stable/package_esp8266com_index.json
 *      - Tools > Board > NodeMCU 0.9 (ESP-12 Module)
 *
 * NodeMCU ESP8266 https://github.com/esp8266
 * Pin mappings: https://techtutorialsx.com/2017/04/02/esp8266-nodemcu-pin-mappings/
 */

const int HC05_RX_PIN = D7;
const int HC05_TX_PIN = D8;
const int HC05_STATE_PIN = D1;
const int HC05_BAUD_RATE = 9600;

const int MOTOR_PIN = D2;
const int STATUS_LED_PIN = D3;

App *app = NULL;

void setup() {
  BluetoothDataStream *stream = new BluetoothDataStream(HC05_RX_PIN, HC05_TX_PIN, HC05_BAUD_RATE);
  ArdPlatform *platform = new ArdPlatform(MOTOR_PIN, STATUS_LED_PIN, HC05_STATE_PIN);
  app = newAppInstance(stream, platform);
}

void loop() {
  app->loop();
}
