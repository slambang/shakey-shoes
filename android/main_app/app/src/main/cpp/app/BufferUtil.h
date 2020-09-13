#ifndef SAMPLES_BUFFERUTIL_H
#define SAMPLES_BUFFERUTIL_H

int nextPowerOfTwo(int in) {
    --in;

    in |= in >> 1;
    in |= in >> 2;
    in |= in >> 4;
    in |= in >> 8;
    in |= in >> 16;

    return in + 1;
}

int getWindowSizeFrames(int intervalMs, int sampleRate) {
    int samplesPerMs = (sampleRate / 1000);
    int samplesPerInterval = samplesPerMs * intervalMs;
    return nextPowerOfTwo(samplesPerInterval);
}

#endif //SAMPLES_BUFFERUTIL_H
