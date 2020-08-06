#include "MagnitudesWriter.h"

MagnitudesWriter::MagnitudesWriter(int sampleRate, int windowSize)
: sampleRate(sampleRate), windowSize(windowSize) {}

MagnitudesWriter::~MagnitudesWriter() {
    close();
}

bool MagnitudesWriter::open(const char *file) {
    if (mFile == nullptr) {
        mFile = fopen(file, "a");
        fprintf(mFile, "%d\n", windowSize / 2);
        fprintf(mFile, "%d\n", sampleRate);
        fprintf(mFile, "%d\n", windowSize);
        return true;
    } else {
        return false;
    }
}

bool MagnitudesWriter::write(const float *data, int channels, int frames) {
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

bool MagnitudesWriter::close() {
    if (mFile != nullptr) {
        fclose(mFile);
        mFile = nullptr;
        return true;
    } else {
        return false;
    }
}
