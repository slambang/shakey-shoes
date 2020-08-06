#include "BeatGeneratorImpl.h"

BeatGeneratorImpl::BeatGeneratorImpl(int windowSize, int sampleRate, float threshold,
                                     SignalFeature feature, Window window) :
                                     sampleRate(sampleRate), event(windowSize / 2) {
    setupGist(windowSize, threshold, feature, window);
}

void BeatGeneratorImpl::setupGist(int windowSize, float threshold, SignalFeature feature, Window window) {
    auto gistWindow = static_cast<WindowType>(window);
    auto gistFeature = static_cast<GIST_FEATURE>(feature);

    mOfxGist = new ofxGist(windowSize, sampleRate, gistWindow);
    mOfxGist->setup();
    mOfxGist->setThreshold(gistFeature, threshold);
    mOfxGist->setUseForOnsetDetection(gistFeature);
    mOfxGist->setDetect(gistFeature, true);
    mOfxGist->setUseForOnsetDetection(gistFeature, true);
}

BeatGeneratorImpl::~BeatGeneratorImpl() {
    delete mOfxGist;
}

Event* BeatGeneratorImpl::generate(float *window, int32_t size) {

    mOfxGist->processAudio(window, size, sampleRate);
    const float *magnitudes = &mOfxGist->getMagnitudeSpectrum()[0];

    GistEvent *gistEvent = &mOfxGist->event;
    event.set(gistEvent->energy, gistEvent->frequency, gistEvent->note, gistEvent->onsetAmount, mOfxGist->_isNoteOn, magnitudes, size / 2);
    return &event;
}
