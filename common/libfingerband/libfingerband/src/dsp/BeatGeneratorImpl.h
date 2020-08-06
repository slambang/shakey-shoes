//
// Created by steve on 22/08/2019.
//

#ifndef DESKTOP_PORT_NATIVE_BEATGENERATORIMPL_H
#define DESKTOP_PORT_NATIVE_BEATGENERATORIMPL_H

#include <ofxGist.h>
#include "Event.h"
#include "BeatGenerator.h"

enum SignalFeature {
    Pitch,
    Note,
    RootMeanSquare,
    PeakEnergy,
    SpectralCrest,
    ZeroCrossingRate,
    SpecialCentroid,
    SpectralFlatness,
    SpectralDifference,
    SpectralDifferenceComplex,
    SpectralDifferenceHalfway,
    HighFrequencyCOntent
};

enum Window {
    Rectangular,
    Hanning,
    Hamming,
    Blackman,
    Tukey
};

class BeatGeneratorImpl : public BeatGenerator {
    public:
        BeatGeneratorImpl(int windowSize, int sampleRate, float threshold, SignalFeature feature, Window window);
        ~BeatGeneratorImpl() override;
        Event* generate(float *window, int32_t size) override;

    private:
        void setupGist(int windowSize, float threshold, SignalFeature feature, Window window);

        int sampleRate { 0 };
        ofxGist *mOfxGist { nullptr };
        Event event;
};

#endif //DESKTOP_PORT_NATIVE_BEATGENERATORIMPL_H
