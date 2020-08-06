#include "AverageMagnitudeWriter.h"

AverageMagnitudeWriter::AverageMagnitudeWriter(int windowSize) : windowSize(windowSize) {}

AverageMagnitudeWriter::~AverageMagnitudeWriter() {
    close();
}

bool AverageMagnitudeWriter::open(const char *file) {
    if (mFile == nullptr) {
        mFile = fopen(file, "a");
        fprintf(mFile, "%d\n", windowSize);
        return true;
    } else {
        return false;
    }
}

bool AverageMagnitudeWriter::write(float value) {
    if (mFile == nullptr) {
        return false;
    } else {
        fprintf(mFile, "%f\n", value);
        return true;
    }
}

bool AverageMagnitudeWriter::close() {
    if (mFile != nullptr) {
        fclose(mFile);
        mFile = nullptr;
        return true;
    } else {
        return false;
    }
}
