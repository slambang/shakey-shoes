#ifndef SAMPLES_FINGERBANDDSPPRODUCER_H
#define SAMPLES_FINGERBANDDSPPRODUCER_H

#include "DataBuffer.h"
#include <wav/WavStreamReader.h>

const long MOCK_DSP_SLEEP_MICROS = MILLIS_TO_MICROS(1);

class FingerbandDspProducer : public DataBuffer::DspProducer {
public:
    class DspListener {
    public:
        virtual void onDspWindowReady(int *buffer, int numValues) = 0;
    };

    virtual ~FingerbandDspProducer() {
        delete[]mWindowBuffer;
    };

    void init(int windowSize, DspListener *listener) {
        mListener = listener;
        mWindowSize = windowSize;
        mWindowBuffer = new int[mWindowSize];
    }

    void produce(float *srcBuffer, int numFrames, int numChannels) override {

        int value = getValue(srcBuffer, numFrames, numChannels);

        mWindowBuffer[mValueCount] = value;

        if (++mValueCount == mWindowSize) {
            mListener->onDspWindowReady(mWindowBuffer, mWindowSize);
            mValueCount = 0;
        }
    }

private:
    int getValue(float *srcBuffer, int numFrames, int numChannel) {
        usleep(MOCK_DSP_SLEEP_MICROS); // mock fingerband call
        return mValue++;
    }

    int mValue{0};

    DspListener *mListener{nullptr};
    int *mWindowBuffer{nullptr};
    int mValueCount{0};
    int mWindowSize{0};
};

#endif //SAMPLES_FINGERBANDDSPPRODUCER_H
