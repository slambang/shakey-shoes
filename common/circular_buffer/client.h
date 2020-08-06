#ifndef APP_PROTOCOL_H
#define APP_PROTOCOL_H

#include "data_stream.h"

class Client {
public:
    class Listener {
    public:
        virtual void onClientConnected() = 0;

        virtual void onClientReset() = 0;

        virtual void onClientConfigReady() = 0;

        virtual void onClientResumed() = 0;

        virtual void onClientPaused() = 0;

        virtual void onClientStreamDataReceived(BYTE data) = 0;
    };

    virtual ~Client() = default;

    virtual void setListener(Listener *listener) = 0;

    virtual void loop() = 0;

    virtual void reset() = 0;

    virtual void onFreeHeapBytes(int freeRamBytes) = 0;

    virtual bool readByte(BYTE &ref, bool blocking) = 0;

    virtual void onReady() = 0;

    virtual void onPaused() = 0;

    virtual void onResumed() = 0;

    virtual void requestRefill() = 0;

    virtual void onUnderflow() = 0;

    virtual int maxUnderflowsConfig() = 0;

    virtual int refillSizeConfig() = 0;

    virtual BYTE numRefillsConfig() = 0;

    virtual BYTE windowSizeMsConfig() = 0;
};

class ClientImpl : public Client {
public:
    // Sent to the client (visible for testing)
    const static BYTE SIGNAL_OUT_READY = 0;
    const static BYTE SIGNAL_OUT_PAUSED = 1;
    const static BYTE SIGNAL_OUT_RESUMED = 2;
    const static BYTE SIGNAL_OUT_REQUEST_REFILL = 3;
    const static BYTE SIGNAL_OUT_UNDERFLOW = 4;

    // Rceived from the client (visible for testing)
    const static BYTE SIGNAL_IN_COMMAND = 255;
    const static BYTE SIGNAL_IN_COMMAND_CONNECT = 2;
    const static BYTE SIGNAL_IN_COMMAND_RESET = 3;
    const static BYTE SIGNAL_IN_COMMAND_CONFIG = 4;
    const static BYTE SIGNAL_IN_COMMAND_RESUME = 5;
    const static BYTE SIGNAL_IN_COMMAND_PAUSE = 6;

    ClientImpl(DataStream *stream, Platform *platform) : stream(stream), platform(platform) {}

    ~ClientImpl() {
        delete stream;
    }

    void setListener(Listener *l) override {
        this->listener = l;
    }

    void loop() override {
        if (!readByte(dataBuffer, false)) return;

        if (dataBuffer == SIGNAL_IN_COMMAND) {
            processCommand();
        } else {
            listener->onClientStreamDataReceived(dataBuffer);
        }
    }

    void reset() override {
        configBufferHead = 0;
    }

    void onFreeHeapBytes(int freeRamBytes) override {
        writeInt(freeRamBytes);
    }

    void onReady() override {
        stream->write(SIGNAL_OUT_READY);
    }

    void onPaused() override {
        stream->write(SIGNAL_OUT_PAUSED);
    }

    void onResumed() override {
        stream->write(SIGNAL_OUT_RESUMED);
    }

    void requestRefill() override {
        stream->write(SIGNAL_OUT_REQUEST_REFILL);
    }

    void onUnderflow() override {
        stream->write(SIGNAL_OUT_UNDERFLOW);
    }

    BYTE numRefillsConfig() override {
        return configBuffer[0];
    }

    int refillSizeConfig() override {
        return DataStream::toInt(&configBuffer[1]);
    }

    BYTE windowSizeMsConfig() override {
        return configBuffer[5];
    }

    int maxUnderflowsConfig() override {
        return DataStream::toInt(&configBuffer[6]);
    }

private:
    bool readByte(BYTE &ref, bool blocking) override {
        if (blocking) {
            while (platform->isConnected() && !stream->available()) { /* wait */ }
            if (!platform->isConnected()) return false;
            ref = stream->read();
            return true;
        } else {
            if (stream->available()) {
                ref = stream->read();
                return true;
            } else {
                return false;
            }
        }
    }

    void processCommand() {

        if (!readByte(dataBuffer, true)) return;

        switch (dataBuffer) {
            case SIGNAL_IN_COMMAND_CONNECT:
                listener->onClientConnected();
                break;
            case SIGNAL_IN_COMMAND_RESET:
                listener->onClientReset();
                break;
            case SIGNAL_IN_COMMAND_CONFIG:
                processConfigData();
                break;
            case SIGNAL_IN_COMMAND_RESUME:
                listener->onClientResumed();
                break;
            case SIGNAL_IN_COMMAND_PAUSE:
                listener->onClientPaused();
                break;
            default:
                break;
        }
    }

    void processConfigData() {
        if (!readByte(dataBuffer, true)) return;
        configBuffer[configBufferHead++] = dataBuffer;
        if (configBufferHead == 10) {
            // Now it is safe to call all `*Config()` functions
            listener->onClientConfigReady();
        }
    }

    void writeInt(int value) {
        DataStream::toByteArray(value, intByteBuffer);
        stream->write(intByteBuffer[0]);
        stream->write(intByteBuffer[1]);
        stream->write(intByteBuffer[2]);
        stream->write(intByteBuffer[3]);
    }

    DataStream *stream;
    Platform *platform;
    Listener *listener;

    BYTE dataBuffer;
    BYTE configBuffer[10];
    BYTE configBufferHead{0};
    BYTE intByteBuffer[4];
};

#endif // APP_PROTOCOL_H
