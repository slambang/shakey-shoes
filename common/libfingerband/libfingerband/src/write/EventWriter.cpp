#include "EventWriter.h"

EventWriter::EventWriter(int windowsSize, float threshold) : windowsSize(windowsSize), threshold(threshold) {}

EventWriter::~EventWriter() {
    close();
}

bool EventWriter::open(const char *file) {
    if (mFile == nullptr) {
        mFile = fopen(file, "a");
        fprintf(mFile, "%d\n", windowsSize);
        fprintf(mFile, "%.10f\n", threshold);
        return true;
    } else {
        return false;
    }
}

bool EventWriter::write(Event *event) {
    if (mFile == nullptr) {
        return false;
    } else {
        sstream.str("");
        sstream << event->mIsNoteOn << "\n";
        sstream << event->mEnergy << "\n";
        sstream << event->mFrequency << "\n";
        sstream << event->mNote << "\n";
        sstream << event->mOnsetAmount << "\n";

        fprintf(mFile, "%s", sstream.str().c_str());
        return true;
    }
}

bool EventWriter::close() {
    if (mFile != nullptr) {
        fclose(mFile);
        mFile = nullptr;
        return true;
    } else {
        return false;
    }
}
