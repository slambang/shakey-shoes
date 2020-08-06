#ifndef APP_NOCKPLATFORM_H
#define APP_NOCKPLATFORM_H

#include <platform.h>

#include <iostream>
#include <chrono>

class MockPlatform : public Platform {
public:
    int freeHeapBytes() override {
        return 1234567890;
    }

    TIME timeNowMillis() override {
        return std::chrono::duration_cast<std::chrono::milliseconds>(
                std::chrono::system_clock::now().time_since_epoch()).count();
    }

    void onIntervalEvent(int value) override {
//        std::cout << "intervalEvent=" << value << std::endl;
    }

    void setLedEnabled(bool enabled) override {
        std::cout << "deviceConnected=" << (enabled ? "true" : "false") << std::endl;
    }

    bool isConnected() override {
        return true;
    }
};

#endif //APP_NOCKPLATFORM_H
