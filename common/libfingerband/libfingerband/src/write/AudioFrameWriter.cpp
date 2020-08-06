#include "AudioFrameWriter.h"

AudioFrameWriter::~AudioFrameWriter() {
    close();
}

bool AudioFrameWriter::open(const char *file, int totalFrames) {
    if (file == nullptr) {
        return false;
    }

    mFile = fopen(file, "a");
    if (mFile == nullptr) {
        return false;
    }

    if (totalFrames > -1) {
        fprintf(mFile, "%d\n", totalFrames);
    }

    return true;
}

bool AudioFrameWriter::write(const float *data, int channels, int frames) {
    if (mFile == nullptr) {
        return false;
    } else {
        for (int i = 0; i < frames; ++i) {
            for (int j = 0; j < channels; ++j) {
                auto sample = data[(i * channels) + j];
                fprintf(mFile, "%f", sample);
            }

            fprintf(mFile, "\n");
        }

        return true;
    }
}

bool AudioFrameWriter::close() {
    if (mFile != nullptr) {
        fclose(mFile);
        mFile = nullptr;
        return true;
    } else {
        return false;
    }
}
