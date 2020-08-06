#ifndef SAMPLES_AUDIOCONSUMER_H
#define SAMPLES_AUDIOCONSUMER_H

#include "DataBuffer.h"
#include "../../../../shared/IRenderableAudio.h"

class AudioConsumer : public IRenderableAudio {
public:
    void init(DataBuffer *dataBuffer) {
        mDataBuffer = dataBuffer;
    }

    void renderAudio(float *audioData, int numFrames) override {
        mDataBuffer->consumeAudioData(audioData, numFrames);
    }

private:
    DataBuffer *mDataBuffer;
};

#endif //SAMPLES_AUDIOCONSUMER_H
