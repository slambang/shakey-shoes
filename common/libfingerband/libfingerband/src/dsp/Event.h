#ifndef FINGERBAND_EVENT_H
#define FINGERBAND_EVENT_H

#include <sstream>
#include <cstring>

class Event {

public:
    float mEnergy;
    float mFrequency;
    float mNote;
    float mOnsetAmount;
    bool mIsNoteOn;
    float *mMagnitudes;

    Event(int size) {
        mMagnitudes = new float[size];
    }

    void set(float energy, float frequency, float note, float onsetAmount, bool isNoteOn, const float *magnitudes, int size) {
        this->mEnergy = energy;
        this->mFrequency = frequency;
        this->mNote = note;
        this->mOnsetAmount = onsetAmount;
        this->mIsNoteOn = isNoteOn;

        memcpy(mMagnitudes, magnitudes, size);
    }
};

#endif //FINGERBAND_EVENT_H
