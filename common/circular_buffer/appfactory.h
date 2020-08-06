#ifndef APP_APPFACTORY_H
#define APP_APPFACTORY_H

#include "app.h"

App *newAppInstance(DataStream *stream, Platform *platform) {

    Client *client = new ClientImpl(stream, platform);
    CircularBuffer *buffer = new CircularBufferImpl;
    IntervalTimer *bufferTimer = new IntervalTimerImpl(platform);

    IntervalTimer *ledTimer = new IntervalTimerImpl(platform);
    ledTimer->setInterval(1000);

    return new AppImpl(client, platform, buffer, bufferTimer, ledTimer);
}

#endif //APP_APPFACTORY_H
