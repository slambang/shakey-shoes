#include "circular_buffer/data_stream.h"
#include <SoftwareSerial.h>

class BluetoothDataStream : public DataStream {
public:
  BluetoothDataStream(int rx, int tx, int baud) {
    bluetooth = new SoftwareSerial(rx, tx);
    bluetooth->begin(baud);
  }

  bool available() override {
    return bluetooth->available();
  }

  int read() override {
    return bluetooth->read();
  }

  void write(int data) override {
    bluetooth->write(data);
  }

private:
  SoftwareSerial *bluetooth;
};
