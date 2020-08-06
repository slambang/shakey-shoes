#include <jni.h>
#include <AndroidBuffer.h>
#include <PreProcessor.h>

extern "C" {

jint JNI_OnLoad(JavaVM *, void *) {
    std::cout.rdbuf(new AndroidBuffer);
    return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *, void *) {
    delete std::cout.rdbuf(0); // TODO: This is suspicious!
}

class BeatReceiver : public WindowResultReceiver {
    void onWindowResult(WindowResult *result) override {

    }
};

JNIEXPORT jint JNICALL
Java_com_betty7_fingerband_alpha_audio_JniBridgeImpl_runFingerbandNative(JNIEnv *env, jobject, jint filterCenterHz, jint filterWidthHz, jint bufferSize, jfloat threshold, jint windowName, jint signalFeature, jstring inputFile, jstring outputDir) {

    const char *nativeInputFile = env->GetStringUTFChars(inputFile, nullptr);
    const char *nativeOutputDir = env->GetStringUTFChars(outputDir, nullptr);

    BeatProcessorConfig config;
    config.signalFeature = (SignalFeature)signalFeature;
    config.windowType = (Window)windowName;
    config.frameBufferSize = bufferSize;
    config.threshold = threshold;
    config.filterCenterHz = filterCenterHz;
    config.filterWidthHz = filterWidthHz;

    PreProcessor preProcessor;
    BeatReceiver beatReceiver;
    int result = preProcessor.preProcess(nativeInputFile, nativeOutputDir, &config, &beatReceiver);
    switch (result) {
        case 0: break; // no-op
        case -1: cout << "Input wav error."; break;
        case -2: cout << "Debug log error."; break;
        default: cout << "Unknown error."; break;
    }

    env->ReleaseStringUTFChars(inputFile, nativeInputFile);
    env->ReleaseStringUTFChars(outputDir, nativeOutputDir);

    return result;
}

} // extern "C"
