#include "WavFileDataSource.h"

#define DEFAULT_FRAME_BUFFER_SIZE 200

bool WavFileDataSource::open(const char* file) {

    mFile = sf_open(file, SFM_READ, &mInfo);
    if (mFile == nullptr) {
        return false;
    }

    mFrameBuffer.resize((unsigned long)DEFAULT_FRAME_BUFFER_SIZE * mInfo.channels);
    return true;
}

bool WavFileDataSource::close() {
    if (mFile) {
        sf_close(mFile);
        mFile = nullptr;
        return true;
    }
    return false;
}

WavFileDataSource::~WavFileDataSource() {
    close();
}

int WavFileDataSource::read(float*& buffer, int readFrames) {

    checkFrameBufferSize(readFrames * mInfo.channels);

    sf_count_t frame = 0;
    size_t  numFrames = 0;
    while ((frame < readFrames) && (frame < mInfo.frames)) {

        numFrames = sf_readf_float(mFile, &mFrameBuffer[0], readFrames);

        if (numFrames != readFrames) {
            break;
        } else {
            frame += numFrames;
        }
    }

    buffer = &mFrameBuffer[0];
    return numFrames;
}

void WavFileDataSource::checkFrameBufferSize(size_t size) {
    if (mFrameBuffer.capacity() < size) {
        mFrameBuffer.reserve((unsigned long)size);
    }
}
