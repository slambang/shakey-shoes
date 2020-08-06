#ifndef DESKTOP_PORT_NATIVE_DSPUTIL_H
#define DESKTOP_PORT_NATIVE_DSPUTIL_H

#include <cstdio>
#include <cstdint>
#include <Iir.h>

class DspUtil {
public:
    static int64_t timeNow();
    static void toMono(float *input, float *output, size_t numFrames, size_t numChannels);
    static void filter(Iir::RBJ::RBJbase *filter, int numFrames, const float *window, float *output);
    static void normaliseMagnitudes(float *window, int size);
    static float averageMagnitude(int centerHz, int widthHz, const float *magnitudes, int sampleRate, int frameBufferSize);
};

#endif //DESKTOP_PORT_NATIVE_DSPUTIL_H
