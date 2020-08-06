#include "DataBuffer.h"
#include <unistd.h>
#include <memory>

/**
 * TODO
 * Single responsibility
 * This class should only be responsible for the threading between producers/consumers.
 * Move the audio buffer stuff to AudioProducer.
 *
 */

const long REFILL_SLEEP_MICROS = MILLIS_TO_MICROS(2);

DataBuffer::~DataBuffer() {
    reset();
}

void DataBuffer::setProducers(AudioProducer *audioProducer, DspProducer *dspProducer) {
    mAudioProducer = audioProducer;
    mDspProducer = dspProducer;
}

void DataBuffer::init(int refillSizeFrames,
                      int maxRefills,
                      int numChannels) {
    reset();

    mRefillSizeFrames = refillSizeFrames;
    mAudioBufferSizeSamples = (mRefillSizeFrames * maxRefills) * numChannels;
    mAudioBuffer = new float[mAudioBufferSizeSamples];
}

void DataBuffer::reset() {
    stop();
    delete[]mAudioBuffer;
    mAudioBuffer = nullptr;
}

int DataBuffer::getRefillSizeFrames() {
    return mRefillSizeFrames;
}

void DataBuffer::start() {
    if (!mThread) {
        pthread_create(&mThread, nullptr, &DataBuffer::threadLoop, this);
    }
}

void DataBuffer::primeData(int refills) {

    mAudioReadHead = 0;
    mAudioWriteHead = 0;

    for (int i = 0; i < refills; ++i) {
        produceData(this);
    }
    mNumEmptyRefills = 0;
}

void DataBuffer::stop() {
    if (!mAlive) return;
    mAlive = false;
    pthread_join(mThread, nullptr);
    mThread = 0;
}

void DataBuffer::produceData(DataBuffer *it) {
    produceAudioData(it);
    produceDspData(it);
    __sync_fetch_and_sub(&it->mNumEmptyRefills, 1);
}

void DataBuffer::produceAudioData(DataBuffer *it) {
    int totalFramesRead = it->mAudioProducer->read(&it->mAudioBuffer[it->mAudioWriteHead], it->mRefillSizeFrames);
    if (totalFramesRead == 0) {
        memset(&it->mAudioBuffer[it->mAudioWriteHead], 0, it->mRefillSizeFrames * it->mAudioProducer->getNumChannels() * sizeof(float));
    }

    int totalSamplesRead = totalFramesRead * it->mAudioProducer->getNumChannels();
    it->mAudioWriteHead += totalSamplesRead;
    if (it->mAudioWriteHead == it->mAudioBufferSizeSamples) {
        it->mAudioWriteHead = 0;
    }
}
void DataBuffer::consumeAudioData(float *audioData, int32_t numFrames) {

    if (mIsPlaying) {
        int numSamplesToRead = numFrames * mAudioProducer->getNumChannels();

        memcpy(audioData, &mAudioBuffer[mAudioReadHead], numSamplesToRead * sizeof(float));
        mAudioReadHead += numSamplesToRead;
        if (mAudioReadHead == mAudioBufferSizeSamples) {
            mAudioReadHead = 0;
        }

        __sync_fetch_and_add(&mNumEmptyRefills, 1);
    } else {
        memset(audioData, 0, numFrames * mAudioProducer->getNumChannels() * sizeof(float));
    }
}

void DataBuffer::produceDspData(DataBuffer *it) {
    it->mDspProducer->produce(
            &it->mAudioBuffer[it->mAudioReadHead],
            it->mRefillSizeFrames,
            it->mAudioProducer->getNumChannels());
}

void *DataBuffer::threadLoop(void *context) {

    auto *it = (DataBuffer *) context;
    it->mAlive = true;

    while (it->mAlive) {
        usleep(REFILL_SLEEP_MICROS);
        if (!it->mAlive) break;

        while ((__sync_fetch_and_add(&it->mNumEmptyRefills, 0) > 0) && it->mAlive) {
            it->produceData(it);
        }
    }

    return nullptr;
}
