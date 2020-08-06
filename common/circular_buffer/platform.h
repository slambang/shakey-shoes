#ifndef APP_PLATFORM_H
#define APP_PLATFORM_H

typedef unsigned long TIME;
typedef unsigned char BYTE;

class Platform {
public:
    virtual ~Platform() = default;

    virtual int freeHeapBytes() = 0;

    virtual TIME timeNowMillis() = 0;

    virtual void onIntervalEvent(int value) = 0;

    virtual void setLedEnabled(bool enabled) = 0;

    virtual bool isConnected() = 0;
};

#endif //APP_PLATFORM_H
