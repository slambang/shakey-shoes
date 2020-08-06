#ifndef PREPROCESSOR_H
#define PREPROCESSOR_H

#include "BeatProcessor.h"
#include <decode/WavFileDataSource.h>

#ifdef BEATPROCESSOR_DEBUG
#include <DebugLogger.h>
#endif

class WindowResultReceiver {
public:
    virtual void onWindowResult(WindowResult *result) {};
};

class PreProcessor {
    public:
        int preProcess(const char *inputFile, const char *outputDirectory, BeatProcessorConfig *config, WindowResultReceiver *receiver);

    private:
        int analyse(WavFileDataSource &wav, BeatProcessorConfig &config, WindowResultReceiver *receiver);

#ifdef BEATPROCESSOR_DEBUG
    DebugLogger *logger = nullptr;
#endif
};

#endif //PREPROCESSOR_H

