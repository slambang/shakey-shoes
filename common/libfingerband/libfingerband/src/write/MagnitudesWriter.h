#ifndef DESKTOP_PORT_NATIVE_MAGNITUDESWRITER_H
#define DESKTOP_PORT_NATIVE_MAGNITUDESWRITER_H

#include "File.h"
#include <cstdio>

// This class is not thread safe
class MagnitudesWriter : public File {
public:
    explicit MagnitudesWriter(int sampleRate, int windowSize);
    ~MagnitudesWriter() override;

    bool open(const char *file) override;
    bool write(const float *data, int channels, int numFrames);
    bool close() override;

private:
    int sampleRate { 0 };
    int windowSize { 0 };
    FILE *mFile { nullptr };
};

#endif //DESKTOP_PORT_NATIVE_MAGNITUDESWRITER_H
