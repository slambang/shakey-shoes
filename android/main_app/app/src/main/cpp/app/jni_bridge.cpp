#include <jni.h>
#include <oboe/Oboe.h>
#include "OboeBridge.h"
#include "BufferUtil.h"
#include "DataBuffer.h"
#include "WavAudioProducer.h"
#include "FingerbandDspProducer.h"
#include "AudioConsumer.h"
#include "DspJniPusher.h"

struct App {

    OboeBridge mOboeBridge;

    DataBuffer mDataBuffer;

    FingerbandDspProducer mDspProducer;

    WavAudioProducer mAudioProducer;

    AudioConsumer mAudioConsumer;

    DspJniPusher mDspPusher;
};

App *initApp(jobject obj, JNIEnv *jniEnv, const char *path, int intervalMs, int maxRefills, int refillSize, int* dspBuffer, int dspBufferSize) {

    auto *app = new App();

    // Initialise producers and buffer
    app->mAudioProducer.init(path);
    int windowSizeFrames = getWindowSizeFrames(intervalMs, app->mAudioProducer.getSampleRate());
    app->mDspProducer.init(refillSize, &app->mDspPusher);
    app->mDataBuffer.init(windowSizeFrames, maxRefills, app->mAudioProducer.getNumChannels());

    // Link producers/consumers via buffer
    app->mDspPusher.init(obj, jniEnv, dspBuffer, dspBufferSize);
    app->mAudioConsumer.init(&app->mDataBuffer);
    app->mDataBuffer.setProducers(&app->mAudioProducer, &app->mDspProducer);

    // Link buffer with Oboe (audio consumer)
    auto *renderable = reinterpret_cast<IRenderableAudio *>(&app->mAudioConsumer);
    app->mOboeBridge.init(app->mDataBuffer.getRefillSizeFrames(), app->mAudioProducer.getSampleRate(), app->mAudioProducer.getNumChannels(), renderable);

    return app;
}

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_slambang_shakeyshoes_oboe_NativeApp_nativeInitApp(
        JNIEnv *env,
        jobject obj,
        jstring path,
        jint intervalMs,
        jint maxRefills,
        jint refillSize,
        jobject dspBuffer) {

    const char *nativePath = env->GetStringUTFChars(path, NULL);
    int *nativeDspBuffer = (int*)env->GetDirectBufferAddress(dspBuffer);
    int nativeDspBufferSize = env->GetDirectBufferCapacity(dspBuffer);

    App *app = initApp(obj, env, nativePath, intervalMs, maxRefills, refillSize, nativeDspBuffer, nativeDspBufferSize);

    env->ReleaseStringUTFChars(path, nativePath);

    return reinterpret_cast<jlong>(app);
}

JNIEXPORT jint JNICALL
Java_com_slambang_shakeyshoes_oboe_NativeApp_nativeGetRefillSizeFrames(
        JNIEnv *,
        jobject,
        jlong appHandle) {

    auto *app = reinterpret_cast<App *>(appHandle);
    return app->mDataBuffer.getRefillSizeFrames();
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_slambang_shakeyshoes_oboe_NativeApp_nativeGetSampleRate(JNIEnv*,
                                                                     jobject,
                                                                     jlong appHandle) {
    auto *app = reinterpret_cast<App *>(appHandle);
    return app->mAudioProducer.getSampleRate();
}

JNIEXPORT void JNICALL
Java_com_slambang_shakeyshoes_oboe_NativeApp_nativeDeleteApp(
        JNIEnv *,
        jobject,
        jlong appHandle) {

    auto *app = reinterpret_cast<App *>(appHandle);
    app->mDataBuffer.stop();
    app->mDspPusher.detach();
    delete reinterpret_cast<App *>(appHandle);
}

JNIEXPORT void JNICALL
Java_com_slambang_shakeyshoes_oboe_NativeApp_nativeSetPlaying(
        JNIEnv *,
        jobject,
        jlong appHandle,
        jboolean isToneOn) {

    auto *app = reinterpret_cast<App *>(appHandle);
    app->mDataBuffer.setIsPlaying(isToneOn);
}

JNIEXPORT jdouble JNICALL
Java_com_slambang_shakeyshoes_oboe_NativeApp_nativeGetCurrentOutputLatencyMillis(
        JNIEnv *,
        jobject,
        jlong appHandle) {

    auto *app = reinterpret_cast<App *>(appHandle);
    return static_cast<jdouble>(app->mOboeBridge.getCurrentOutputLatencyMillis());
}

JNIEXPORT jboolean JNICALL
Java_com_slambang_shakeyshoes_oboe_NativeApp_nativeIsLatencyDetectionSupported(
        JNIEnv *,
        jobject,
        jlong appHandle) {

    auto *app = reinterpret_cast<App *>(appHandle);
    return (app->mOboeBridge.isLatencyDetectionSupported() ? JNI_TRUE : JNI_FALSE);
}

JNIEXPORT void JNICALL
Java_com_slambang_shakeyshoes_oboe_NativeApp_nativePrime(
        JNIEnv *,
        jobject,
        jint refills,
        jlong appHandle) {

    auto *app = reinterpret_cast<App *>(appHandle);
    app->mDataBuffer.primeData(refills);
    app->mDspPusher.prepareForNativeThreadAttach();
}

JNIEXPORT void JNICALL
Java_com_slambang_shakeyshoes_oboe_NativeApp_nativeStart(
        JNIEnv *,
        jobject,
        jlong appHandle) {

    auto *app = reinterpret_cast<App *>(appHandle);
    app->mDataBuffer.start();
}
} // extern "C"
