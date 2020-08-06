#ifndef UNTITLED1_AUDIOFRAMEWRITER_H
#define UNTITLED1_AUDIOFRAMEWRITER_H

#include "File.h"
#include <cstdio>

// This class is not thread safe
class AudioFrameWriter : public File {
public:
    ~AudioFrameWriter() override;

    bool open(const char *file, int totalFrames = -1); // fixme: Not overridden
    bool write(const float *data, int channels, int numFrames);
    bool close() override;

private:
    FILE *mFile { nullptr };
    long mTotalFrames { 0 };
};

#endif //UNTITLED1_AUDIOFRAMEWRITER_H
