#ifndef DESKTOP_PORT_NATIVE_STRINGWRITER_H
#define DESKTOP_PORT_NATIVE_STRINGWRITER_H

#include "File.h"
#include <cstdio>

// This class is not thread safe
class StringWriter : public File {
public:
    StringWriter(){};
    ~StringWriter() override;

    bool open(const char *file) override;
    bool write(const char* data);
    bool close() override;

private:
    FILE *mFile { nullptr };
};

#endif //DESKTOP_PORT_NATIVE_STRINGWRITER_H
