#ifndef AVERAGEMAGNITUDESWRITER_H
#define AVERAGEMAGNITUDESWRITER_H

#include "File.h"
#include <cstdio>

// This class is not thread safe
class AverageMagnitudeWriter : public File {

    public:
        explicit AverageMagnitudeWriter(int windowSize);
        ~AverageMagnitudeWriter() override;

        bool open(const char *file) override;
        bool write(float value);
        bool close() override;

    private:
        FILE *mFile { nullptr };
        int windowSize { 0 };
};

#endif //AVERAGEMAGNITUDESWRITER_H
