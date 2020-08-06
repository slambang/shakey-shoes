/*
 * Copyright (C) 2018 The Android Open Source Project
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
#ifndef FINGERBAND_AUDIOSOURCE_H
#define FINGERBAND_AUDIOSOURCE_H

#include <cstdint>

class AudioDataSource {
public:
    virtual ~AudioDataSource(){};
    virtual int32_t  getSampleRate() const = 0;
    virtual int32_t getTotalFrames() const = 0;
    virtual int32_t getChannelCount() const  = 0;
    virtual int read(float*& buffer, int readFrames) = 0;
};

#endif // TAPPERV_AUDIOSOURCE_H
