#ifndef DESKTOP_PORT_NATIVE_EVENTWRITER_H
#define DESKTOP_PORT_NATIVE_EVENTWRITER_H

#include "File.h"
#include <cstdio>
#include "../dsp/Event.h"

// This class is not thread safe
class EventWriter : public File {
public:
    EventWriter(int windowSize, float threshold);
    ~EventWriter() override;

    bool open(const char *file) override;
    bool write(Event *event);
    bool close() override;

private:
    int windowsSize { 0 };
    float threshold { 0.f };
    FILE *mFile { nullptr };
    std::stringstream sstream;
};

#endif //DESKTOP_PORT_NATIVE_EVENTWRITER_H
