#include <PreProcessor.h>
#include <dsp/DspUtil.h>

// TODO: Add callback that receives each WindowResult!
int PreProcessor::analyse(WavFileDataSource &wav, BeatProcessorConfig &config, WindowResultReceiver *receiver) {

    BeatProcessor beatProcessor(&config);

    int numFrames;
    int totalFrames = 0;
    float *window = nullptr;

#ifdef BEATPROCESSOR_DEBUG
    int64_t totalReads = 0;
    int64_t beatProcessorTotalTime = 0;
    int64_t dataSourceReadTotalTime = 0;
#endif

    while (true) {

#ifdef BEATPROCESSOR_DEBUG
        int64_t dataSourceReadStart = DspUtil::timeNow();
#endif

        numFrames = wav.read(window, config.frameBufferSize);

#ifdef BEATPROCESSOR_DEBUG
        ++totalReads;
        dataSourceReadTotalTime += DspUtil::timeNow() - dataSourceReadStart;
#endif

        if (numFrames <= 0) break;
        totalFrames += numFrames;

#ifdef BEATPROCESSOR_DEBUG
        int64_t processorStart = DspUtil::timeNow();
#endif
        WindowResult *result = beatProcessor.process(window, numFrames);
        receiver->onWindowResult(result);

#ifdef BEATPROCESSOR_DEBUG
        beatProcessorTotalTime += DspUtil::timeNow() - processorStart;
        logger->addResult(window, beatProcessor.getDebugLog());
#endif
    }

#ifdef BEATPROCESSOR_DEBUG
    logger->onFinish(&config, beatProcessor.getDebugLog(), beatProcessorTotalTime, totalReads, dataSourceReadTotalTime);
#endif

    return 0;
}

int PreProcessor::preProcess(const char *inputFile, const char *outputDirectory, BeatProcessorConfig *config, WindowResultReceiver *receiver) {

    WavFileDataSource wav;
    if (!wav.open(inputFile)) {
        return -1;
    }

    // We will need this information for RealTime mode as well!
    config->sampleRate = wav.getSampleRate();
    config->channels = wav.getChannelCount();
    config->totalFrames = wav.getTotalFrames();

#ifdef BEATPROCESSOR_DEBUG
    logger = new DebugLogger(inputFile, outputDirectory, config);
    if (logger->getStatus() != DebugLogger::SUCCESS) {
        return -2;
    }
#endif

#ifndef BEATPROCESSOR_DEBUG
    int64_t processorStart = DspUtil::timeNow();
#endif

    int result = analyse(wav, *config, receiver);

#ifdef BEATPROCESSOR_DEBUG
    delete logger;
#else
    int64_t processorTotal = DspUtil::timeNow() - processorStart;
    cout<<"Processor time: "<<processorTotal<<"ms" <<endl;
#endif

    return result;
}

