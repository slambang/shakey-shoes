#include <iostream>
#include <PreProcessor.h>

class BeatReceiver : public WindowResultReceiver {
    void onWindowResult(WindowResult *result) override {

    }
};

int main(int argc, char **argv) {

    const char *inputFile = argv[1];
    const char *outputDirectory = argv[2];

    BeatProcessorConfig config;
    config.threshold = (float)atof(argv[3]);
    config.frameBufferSize = atoi(argv[4]);
    config.filterCenterHz = atoi(argv[5]);
    config.filterWidthHz = atoi(argv[6]);
    config.windowType = (Window)atoi(argv[7]);
    config.signalFeature = (SignalFeature)atoi(argv[8]);

    PreProcessor preProcessor;
    BeatReceiver beatReceiver;
    int result = preProcessor.preProcess(inputFile, outputDirectory, &config, &beatReceiver);

    switch (result) {
        case 0: break; // no-op
        case -1: cout << "Input wav error: " << inputFile; break;
        case -2: cout << "Debug log error."; break;
        default: cout << "Unknown error."; break;
    }

    return result;
}
