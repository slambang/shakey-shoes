#include <cinttypes>
#include <memory>
#include "OboeBridge.h"
#include <aaudio/AAudio.h>

OboeBridge::~OboeBridge() {
    delete mLatencyCallback;
}

double OboeBridge::getCurrentOutputLatencyMillis() {
    if (!mIsLatencyDetectionSupported) return -1;
    // Get the time that a known audio frame was presented for playing
    auto result = mStream->getTimestamp(CLOCK_MONOTONIC);
    double outputLatencyMillis = -1;
    const int64_t kNanosPerMillisecond = 1000000;
    if (result == oboe::Result::OK) {
        oboe::FrameTimestamp playedFrame = result.value();
        // Get the write index for the next audio frame
        int64_t writeIndex = mStream->getFramesWritten();
        // Calculate the number of frames between our known frame and the write index
        int64_t frameIndexDelta = writeIndex - playedFrame.position;
        // Calculate the time which the next frame will be presented
        int64_t frameTimeDelta =
                (frameIndexDelta * oboe::kNanosPerSecond) / (mStream->getSampleRate());
        int64_t nextFramePresentationTime = playedFrame.timestamp + frameTimeDelta;
        // Assume that the next frame will be written at the current time
        using namespace std::chrono;
        int64_t nextFrameWriteTime =
                duration_cast<nanoseconds>(steady_clock::now().time_since_epoch()).count();
        // Calculate the latency
        outputLatencyMillis = static_cast<double>(nextFramePresentationTime - nextFrameWriteTime)
                              / kNanosPerMillisecond;
    } else {
        LOGE("Error calculating latency: %s", oboe::convertToText(result.error()));
    }
    return outputLatencyMillis;
}

bool OboeBridge::isLatencyDetectionSupported() {
    return mIsLatencyDetectionSupported;
}

void OboeBridge::updateLatencyDetection() {
    mIsLatencyDetectionSupported = (mStream->getTimestamp((CLOCK_MONOTONIC)) !=
                                    oboe::Result::ErrorUnimplemented);
}

void OboeBridge::updateAudioSource() {
    mStream->start();
    updateLatencyDetection();
}

oboe::Result OboeBridge::createPlaybackStream(oboe::AudioStreamBuilder builder) {
    return builder.setSharingMode(oboe::SharingMode::Exclusive)
            ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setFormat(oboe::AudioFormat::Float)
            ->setAudioApi(oboe::AudioApi::Unspecified)
            ->setDeviceId(oboe::kUnspecified)
            ->setContentType(oboe::ContentType::Music)
            ->setCallback(mLatencyCallback)
            ->openManagedStream(mStream);
}

void OboeBridge::restart() {
    init(mFramesPerCallback, mSampleRate, mNumChannels, mRenderable);
}

void OboeBridge::init(int framesPerCallback, int sampleRate, int numChannels, IRenderableAudio* renderable) {

    mFramesPerCallback = framesPerCallback;
    mSampleRate = sampleRate;
    mNumChannels = numChannels;
    mRenderable = renderable;
    mLatencyCallback = new LatencyTuningCallback(*this);
    mLatencyCallback->setSource(renderable);

    auto result = createPlaybackStream(*oboe::AudioStreamBuilder()
            .setSampleRate(sampleRate)
            ->setChannelCount(numChannels)
            ->setFramesPerCallback(framesPerCallback));

    if (result == oboe::Result::OK) {
        updateAudioSource();
        LOGE("Playback stream created");
    } else {
        LOGE("Error creating playback stream. Error: %s", oboe::convertToText(result));
    }
}
