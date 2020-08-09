#include "DspUtil.h"
#include <chrono>
#include <cstring>

using namespace std::chrono;

int64_t DspUtil::timeNow() {
    return duration_cast<milliseconds>(system_clock::now().time_since_epoch()).count();
}

void DspUtil::toMono(float *input, float *output, size_t numFrames, size_t numChannels) {

    if (numChannels == 1) {
        std::memcpy(input, output, sizeof *output);
    } else {
        for (size_t i = 0; i < numFrames; ++i) {

            double total = 0;
            size_t frameOffset = i * numChannels;

            for (size_t j = 0; j < numChannels; ++j) {

                size_t sampleOffset = frameOffset + j;
                total += input[sampleOffset]; // TODO: Wrap-around?
            }
            auto average = float(total / numChannels);

            output[i] = average;
        }
    }
}

void DspUtil::filter(Iir::RBJ::RBJbase *filter, int numFrames, const float *window, float *output) {
    float max = 0.f;
    for (int i = 0; i < numFrames; ++i) {
        auto filteredSample = filter->filter(window[i]);
        output[i] = filteredSample;
        if (filteredSample > max) max = filteredSample;
    }
}

float DspUtil::averageMagnitude(int centerHz, int widthHz, const float *magnitudes, int sampleRate, int frameBufferSize) {
    int bandWidth = (sampleRate / 2);
    int binCount = (frameBufferSize / 2); // TODO: Should we be ignoring DC @ [0]?
    int binWidthHz = bandWidth / binCount;

    int lowerHz = centerHz - widthHz;
    if (lowerHz < 0) lowerHz = 0;

    int upperHz = centerHz + widthHz;
    if (upperHz > bandWidth) upperHz = bandWidth;

    int lowerBinIndex = lowerHz / binWidthHz;
    int upperBinIndex = upperHz / binWidthHz;

    double average = 0;
    if (lowerBinIndex == upperBinIndex) {
        average = magnitudes[lowerBinIndex];
    }
    else {
        for (int i = lowerBinIndex; i < upperBinIndex; ++i) {
            average += magnitudes[i]; // TODO: Wrap-around?
        }
        average /= (float)(upperBinIndex - lowerBinIndex);
    }

    return (float)average;
}