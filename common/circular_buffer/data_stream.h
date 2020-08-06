#ifndef APP_BYTESTREAM_H
#define APP_BYTESTREAM_H

class DataStream {
public:
    static void toByteArray(unsigned int value, BYTE *bytes) {
        bytes[0] = ((value >> 24) & 0xFF);
        bytes[1] = ((value >> 16) & 0xFF);
        bytes[2] = ((value >> 8) & 0XFF);
        bytes[3] = (value & 0XFF);
    }

    static int toInt(BYTE *bytes) {
        return ((bytes[0] << 24)
                + (bytes[1] << 16)
                + (bytes[2] << 8)
                + (bytes[3]));
    }

    virtual ~DataStream() = default;

    virtual bool available() = 0;

    virtual int read() = 0;

    virtual void write(int data) = 0;
};

#endif //APP_BYTESTREAM_H
