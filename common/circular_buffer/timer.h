#ifndef APP_TIMER_H
#define APP_TIMER_H

#include "platform.h"

class IntervalTimer {
public:
    virtual ~IntervalTimer() = default;

    virtual void setInterval(TIME intervalMs) = 0;

    virtual bool isIntervalNow() = 0;

    virtual void start() = 0;

    virtual void pause() = 0;

    virtual void reset() = 0;
};

class IntervalTimerImpl : public IntervalTimer {
public:
    explicit IntervalTimerImpl(Platform *platform) : platform(platform) {}

    void setInterval(TIME interval) override {
        intervalMs = interval;
    }

    // TODO: This doesn't account for the time between start/pause!
    bool isIntervalNow() override {
        if (!started) return false;

        TIME now = platform->timeNowMillis();
        TIME delta = now - lastTime;

        if (delta >= intervalMs) {
            lastTime = now;
            return true;
        } else {
            return false;
        }
    }

    void start() override {
        lastTime = platform->timeNowMillis();
        started = true;
    }

    void pause() override {
        started = false;
    }

    void reset() override {
        started = false;
        lastTime = 0;
    }

private:
    bool started{false};
    TIME lastTime{0};

    TIME intervalMs{0};
    Platform *platform;
};

#endif //APP_TIMER_H
