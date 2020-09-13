/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef OBOE_HELLO_OBOE_ENGINE_H
#define OBOE_HELLO_OBOE_ENGINE_H

#include <oboe/Oboe.h>

#include "LatencyTuningCallback.h"
#include "IRestartable.h"

class OboeBridge : public IRestartable {

public:
    OboeBridge() = default;

    virtual ~OboeBridge();

    // From IRestartable
    void restart() override;

    /**
     * Calculate the current latency between writing a frame to the output stream and
     * the same frame being presented to the audio hardware.
     *
     * Here's how the calculation works:
     *
     * 1) Get the time a particular frame was presented to the audio hardware
     * @see AudioStream::getTimestamp
     * 2) From this extrapolate the time which the *next* audio frame written to the stream
     * will be presented
     * 3) Assume that the next audio frame is written at the current time
     * 4) currentLatency = nextFramePresentationTime - nextFrameWriteTime
     *
     * @return  Output Latency in Milliseconds
     */
    double getCurrentOutputLatencyMillis();

    bool isLatencyDetectionSupported();

    void init(int framesPerCallback, int sampleRate, int numChannels, IRenderableAudio* renderable);

private:
    oboe::Result createPlaybackStream(oboe::AudioStreamBuilder builder);
    void updateLatencyDetection();
    void updateAudioSource();

    int mFramesPerCallback;
    int mSampleRate;
    int mNumChannels;
    IRenderableAudio* mRenderable;

    oboe::ManagedStream mStream;
    LatencyTuningCallback *mLatencyCallback{nullptr};
    bool mIsLatencyDetectionSupported = false;
};

#endif //OBOE_HELLO_OBOE_ENGINE_H
