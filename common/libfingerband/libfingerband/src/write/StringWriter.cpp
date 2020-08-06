#include "StringWriter.h"

StringWriter::~StringWriter() {
    close();
}

bool StringWriter::open(const char *file) {
    if (mFile == nullptr) {
        mFile = fopen(file, "a");
        return true;
    } else {
        return false;
    }
}

bool StringWriter::write(const char *data) {
    if (mFile == nullptr) {
        return false;
    } else {
        fprintf(mFile, "%s", data);
        return true;
    }
}

bool StringWriter::close() {
    if (mFile != nullptr) {
        fclose(mFile);
        mFile = nullptr;
        return true;
    } else {
        return false;
    }
}
