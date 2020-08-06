//
// Created by steve on 22/08/2019.
//

#ifndef DESKTOP_PORT_NATIVE_BEATGENERATOR_H
#define DESKTOP_PORT_NATIVE_BEATGENERATOR_H

#include <cstdint>

class BeatGenerator {
    public:
        virtual ~BeatGenerator(){};
        virtual Event* generate(float *window, int32_t size) = 0;
};

#endif //DESKTOP_PORT_NATIVE_BEATGENERATOR_H
