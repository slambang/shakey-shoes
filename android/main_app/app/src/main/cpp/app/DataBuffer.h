#ifndef SAMPLES_DATABUFFER_H
#define SAMPLES_DATABUFFER_H

#include <pthread.h>

#define MILLIS_TO_MICROS(millis) millis * 1000

class DataBuffer {
public:
    class AudioProducer {
    public:
        virtual int getNumChannels() = 0;

        virtual int getSampleRate() = 0;

        virtual int read(float *buffer, int nuFrames) = 0;
    };

    class DspProducer {
    public:
        virtual void produce(float *buffer, int numFrames, int numChannels) = 0;
    };

    DataBuffer() = default;

    ~DataBuffer();

    void consumeAudioData(float *audioData, int32_t numFrames);

    void setProducers(AudioProducer *audioProducer,
                      DspProducer *dspProducer);

    void init(int refillSizeFrames,
              int maxRefills,
              int numChannels);

    void primeData(int maxRefills);

    void start();

    void stop();

    void setIsPlaying(bool isPlaying) {
        mIsPlaying = isPlaying;
    }

    int getRefillSizeFrames();

private:
    static void *threadLoop(void *context);

    static void produceData(DataBuffer *it);

    static void produceAudioData(DataBuffer *it);

    static void produceDspData(DataBuffer *it);

    void reset();

    bool mIsPlaying{false};

    float *mAudioBuffer{nullptr};
    int mAudioBufferSizeSamples{0};
    int mAudioReadHead{0};
    int mAudioWriteHead{0};
    int mRefillSizeFrames{0};

    /*
     * Not atomic since any other value means "die".
     * This will save us many synchronized comparisons in threadLoop.
     */
    bool mAlive{false};
    pthread_t mThread{0};
    volatile int mNumEmptyRefills{0};

    AudioProducer *mAudioProducer{nullptr};
    DspProducer *mDspProducer{nullptr};
};

#endif //SAMPLES_DATABUFFER_H
