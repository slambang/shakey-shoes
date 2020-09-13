#ifndef SAMPLES_WAVAUDIOPRODUCER_H
#define SAMPLES_WAVAUDIOPRODUCER_H

#include <fcntl.h>

#include "DataBuffer.h"
#include <wav/WavStreamReader.h>
#include <stream//FileInputStream.h>

class WavAudioProducer : public DataBuffer::AudioProducer {
public:
    virtual ~WavAudioProducer() {
        reset();
    }

    void init(const char *path) {
        reset();
        mFileHandle = ::open(path, O_RDONLY);
        mFileStream = new parselib::FileInputStream(mFileHandle);
        mWavReader = new parselib::WavStreamReader(mFileStream);
        mWavReader->parse();
    }

    int getNumChannels() override {
        return mWavReader->getNumChannels();
    }

    int getSampleRate() override {
        return mWavReader->getSampleRate();
    }

    int read(float *buffer, int numFrames) override {
        return mWavReader->getDataFloat(buffer, numFrames);
    }

private:
    void reset() {
        if (mFileHandle != 0) {
            close(mFileHandle);
            mFileHandle = 0;
        }

        delete mFileStream;
        mFileStream = nullptr;

        delete mWavReader;
        mWavReader = nullptr;
    }

    int mFileHandle{0};
    parselib::FileInputStream *mFileStream{nullptr};
    parselib::WavStreamReader *mWavReader{nullptr};
};

#endif //SAMPLES_WAVAUDIOPRODUCER_H
