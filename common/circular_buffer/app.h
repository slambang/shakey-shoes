#ifndef APP_APP_H
#define APP_APP_H

#include "timer.h"
#include "buffer.h"
#include "client.h"
#include "platform.h"

class App {
public:
    virtual ~App() = default;

    virtual void loop() = 0;
};

class AppImpl : public App, private Client::Listener {
public:
    AppImpl(Client *client, Platform *platform, CircularBuffer *buffer, IntervalTimer *bufferTimer,
            IntervalTimer *ledTimer)
            : client(client), platform(platform), buffer(buffer), bufferTimer(bufferTimer), ledTimer(ledTimer) {
        client->setListener(this);
    }

    ~AppImpl() override {
        delete buffer;
        delete client;
        delete bufferTimer;
    }

    void loop() override {
        if (platform->isConnected()) {
            client->loop();
            loopBuffer();
            loopLed();
        } else {
            disconnect();
        }
    }

private:
    void onClientConnected() override {
        onClientReset();
        isLedOn = true;
        platform->setLedEnabled(true);
        ledTimer->start();
        auto freeHeapBytes = platform->freeHeapBytes() - 4;
        client->onFreeHeapBytes(freeHeapBytes);
    }

    void onClientReset() override {
        reset();
    }

    void onClientConfigReady() override {
        bufferTimer->reset();
        bufferTimer->setInterval(client->windowSizeMsConfig());
        buffer->initialise(client->refillSizeConfig() * client->numRefillsConfig());
        ledTimer->reset();
        platform->setLedEnabled(true);
        isConnected = true;
        client->onReady();
    }

    void onClientResumed() override {
        client->onResumed();
        bufferTimer->start();
    }

    void onClientPaused() override {
        client->onPaused();
        bufferTimer->pause();
        platform->onIntervalEvent(0);
    }

    void loopBuffer() {

        if (!bufferTimer->isIntervalNow()) return;

        if (buffer->read(dataBuffer)) {
            platform->onIntervalEvent(dataBuffer);
        } else {
            if (++consecutiveUnderflowCount == client->maxUnderflowsConfig()) {
                platform->onIntervalEvent(0);
            }

            client->onUnderflow();
        }

        if (++frameReadCount == client->refillSizeConfig()) {
            client->requestRefill();
            frameReadCount = 0;
        }
    }

    void reset() {
        buffer->reset();
        bufferTimer->reset();
        frameReadCount = 0;
        consecutiveUnderflowCount = 0;
    }

    void disconnect() {
        isConnected = false;
        client->reset();
        buffer->destroy();
        ledTimer->reset();
        bufferTimer->reset();
        platform->setLedEnabled(false);
    }

    void onClientStreamDataReceived(BYTE data) override {
        if (consecutiveUnderflowCount > 0) {
            --consecutiveUnderflowCount;
        } else {
            buffer->write(data);
        }
    }

    void loopLed() {
        if (!ledTimer->isIntervalNow()) return;
        isLedOn = !isLedOn;
        platform->setLedEnabled(isLedOn);
    }

    Client *client;
    Platform *platform;
    CircularBuffer *buffer;
    IntervalTimer *bufferTimer;
    IntervalTimer *ledTimer;

    bool isLedOn{false};
    bool isConnected{false};

    BYTE dataBuffer;
    int frameReadCount{0};
    int consecutiveUnderflowCount{0};
};

#endif //APP_APP_H
