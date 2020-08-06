#ifndef FINGERBAND_SNDFILEDATASOURCE_H
#define FINGERBAND_SNDFILEDATASOURCE_H

#include "AudioDataSource.h"
#include <sndfile.h> // http://www.mega-nerd.com/libsndfile/api.html
#include <vector>

class WavFileDataSource : public AudioDataSource {

    public:
        ~WavFileDataSource() override ;

        bool open(const char* file);
        bool close();

        int32_t getSampleRate() const override { return (int32_t)mInfo.samplerate; }
        int32_t getTotalFrames() const override { return (int32_t)mInfo.frames; }
        int32_t getChannelCount() const override { return mInfo.channels; }
        int read(float*& buffer, int readFrames) override;
    private:
        void checkFrameBufferSize(size_t numFrames);

        SF_INFO mInfo { 0 };
        SNDFILE* mFile {nullptr };
        std::vector<float> mFrameBuffer;
};

#endif // FINGERBAND_SNDFILEDATASOURCE_H
