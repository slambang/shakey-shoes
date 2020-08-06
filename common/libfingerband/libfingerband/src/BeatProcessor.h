#ifndef BEAT_PROCESSOR_H
#define BEAT_PROCESSOR_H

#include <string>

#include <Iir.h>
#include <WindowResult.h>
#include "dsp/BeatGeneratorImpl.h"

struct BeatProcessorConfig {
    // Set by preProcess()
    int sampleRate = 0;
    int channels = 0;
    int32_t totalFrames = 0;

    // Configurable by the app
    float threshold = 0.5;                          // Gist
    SignalFeature signalFeature = RootMeanSquare;   // Gist
    Window windowType = Hanning;                    // Gist
    int frameBufferSize = 1024;
    int filterCenterHz = 0;
    int filterWidthHz = 250;
};

#ifdef BEATPROCESSOR_DEBUG
struct BeatProcessorLog {

    int64_t filterTime = 0;
    int64_t eventTime = 0;
    int64_t averageMagnitudeTime = 0;
    int64_t monoTime = 0;
    int64_t normaliseTime = 0;

    float averageMagnitude = 0.0f;
    float *monoWindow = { nullptr };
    float *filterOutput = { nullptr };
    int numFrames = 0;
    Event *event = { nullptr };
    const float *filterMagnitudes = { nullptr };

    void reset() {

        filterTime = 0;
        eventTime = 0;
        averageMagnitudeTime = 0;
        monoTime = 0;

        monoWindow = nullptr;
        filterOutput = nullptr;
        numFrames = 0;
        event = nullptr;
        filterMagnitudes = nullptr;
    }
};
#endif

class BeatProcessor {
public:
    BeatProcessor(BeatProcessorConfig *configuration);
    virtual ~BeatProcessor();

    WindowResult* process(float *window, int numFrames);

#ifdef BEATPROCESSOR_DEBUG
    BeatProcessorLog* getDebugLog();
#endif

private:
#ifdef BEATPROCESSOR_DEBUG
    float getAverageMagnitude(int lowerHz, int upperHz, const float *magnitudes, int sampleRate, int frameBufferSize);
#else
    float getAverageMagnitude(int lowerHz, int upperHz, const float *magnitudes, int sampleRate, int frameBufferSize);
#endif
    void applyFilter(int numFrames, const float *window, float *output);
    Event* generateEvent(int numFrames);
    void toMono(float *window, int numFrames);
    void onWindow(float *window, int numFrames);

    BeatProcessorConfig *config { nullptr };
    BeatGeneratorImpl *generator { nullptr };
    float *lpfData { nullptr };
    Iir::RBJ::BandPass1 filter; //const int order = 4; // 4th order (=2 biquads)
    WindowResult result;

#ifdef BEATPROCESSOR_DEBUG
    BeatProcessorLog log;
#endif
};

#endif //BEAT_PROCESSOR_H
