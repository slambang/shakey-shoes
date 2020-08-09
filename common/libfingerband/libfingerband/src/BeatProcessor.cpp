#include "BeatProcessor.h"

#include "dsp/DspUtil.h"
#include "WindowResult.h"
#include "dsp/BeatGeneratorImpl.h"

#include <string>

using namespace std;

BeatProcessor::BeatProcessor(BeatProcessorConfig *configuration) : result() {
    config = configuration;
    generator = new BeatGeneratorImpl(config->frameBufferSize, config->sampleRate, config->threshold, config->signalFeature, config->windowType);
    lpfData = new float[config->frameBufferSize];
    filter.setup(config->sampleRate, config->filterCenterHz, config->filterWidthHz);
}

BeatProcessor::~BeatProcessor() {
    delete generator;
    delete []lpfData;
}

void BeatProcessor::applyFilter(int numFrames, const float *window, float *output) {

#ifdef BEATPROCESSOR_DEBUG
    int64_t start = DspUtil::timeNow();
#endif

    DspUtil::filter(&filter, numFrames, window, output);

#ifdef BEATPROCESSOR_DEBUG
    log.filterOutput = output;
    log.filterTime = DspUtil::timeNow() - start;
#endif
}

Event* BeatProcessor::generateEvent(int numFrames) {

#ifdef BEATPROCESSOR_DEBUG
    int64_t start = DspUtil::timeNow();
#endif

    Event *event = generator->generate(lpfData, numFrames);

#ifdef BEATPROCESSOR_DEBUG
    log.event = event;
    log.eventTime = DspUtil::timeNow() - start;
#endif

    return event;
}

void BeatProcessor::toMono(float *window, int numFrames) {

#ifdef BEATPROCESSOR_DEBUG
    int64_t start = DspUtil::timeNow();
#endif

    DspUtil::toMono(window, window, numFrames, config->channels);

#ifdef BEATPROCESSOR_DEBUG
    log.monoTime = DspUtil::timeNow() - start;
    log.monoWindow = window;
#endif
}

float BeatProcessor::getAverageMagnitude(int centerHz, int widthHz, const float *magnitudes, int sampleRate, int frameBufferSize) {

#ifdef BEATPROCESSOR_DEBUG
    log.filterMagnitudes = magnitudes;
    int64_t start = DspUtil::timeNow();
#endif

    float average = DspUtil::averageMagnitude(centerHz, widthHz, magnitudes, sampleRate, frameBufferSize);

#ifdef BEATPROCESSOR_DEBUG
    log.averageMagnitude = average;
    log.averageMagnitudeTime = DspUtil::timeNow() - start;
#endif

    return average;
}

WindowResult* BeatProcessor::process(float *window, int numFrames) {

#ifdef BEATPROCESSOR_DEBUG
    log.reset();
    log.numFrames = numFrames;
#endif

    toMono(window, numFrames);
    applyFilter(numFrames, window, lpfData);
    Event *event = generateEvent(numFrames);
    float averageMagnitude = getAverageMagnitude(config->filterCenterHz, config->filterWidthHz, event->mMagnitudes, config->sampleRate, config->frameBufferSize);

    // TODO: Scale these and we are done!
    result.bassAmount = averageMagnitude;
    result.beatAmount = event->mOnsetAmount;
    return &result;
}

#ifdef BEATPROCESSOR_DEBUG
BeatProcessorLog* BeatProcessor::getDebugLog() {
    return &log;
}
#endif
