#ifndef SAMPLES_DSPJNIPUSHER_H
#define SAMPLES_DSPJNIPUSHER_H

#include "FingerbandDspProducer.h"

class DspJniPusher : public FingerbandDspProducer::DspListener {
public:
    DspJniPusher() = default;

    void init(jobject instance, JNIEnv *jniEnv, int *destinationBuffer, int refillSiz) {
        mDestinationBuffer = destinationBuffer;
        initJni(instance, jniEnv);
    }

    void onDspWindowReady(int *sourceBuffer, int numValues) override {
        memcpy(mDestinationBuffer, sourceBuffer, numValues * sizeof(int));
        notifyJvm();
    }

    void prepareForNativeThreadAttach() {
        mJniEnv = nullptr;
    }

    void detach() {
        mJniEnv->DeleteGlobalRef(mClass);
        mJavaVm->DetachCurrentThread();
    }
private:
    void initJni(jobject instance, JNIEnv *jniEnv) {
        mJniEnv = jniEnv;
        jniEnv->GetJavaVM(&mJavaVm);
        jclass cls = jniEnv->GetObjectClass(instance);
        mClass = (jclass)jniEnv->NewGlobalRef(cls);
        mMethodId = jniEnv->GetStaticMethodID(cls, "onDspBufferReady", "()V");
    }

    void notifyJvm() {
        /*
         * While priming, mJniEnv should be the one already attached to the JVM calling thread,
         * provided by the call to 'init()'.' At first, we Allow priming/JVM callback on that
         * JNIEnv instance.
         *
         * Later, this is called from a separate (native) thread which is not yet attached to the JVM.
         * In that case we must attach before the JNI callback can be made.
         */
        if (mJniEnv == nullptr) {
            // This should only ever happen once (the first time the native thread reaches here).
            if (mJavaVm->AttachCurrentThread(&mJniEnv, NULL) != JNI_OK) {
                throw std::runtime_error("NOT ATTACHED!");
            }
        }
        mJniEnv->CallStaticVoidMethod(mClass, mMethodId);
    }

    JavaVM *mJavaVm{nullptr};
    JNIEnv *mJniEnv{nullptr};
    jclass mClass{nullptr};
    jmethodID mMethodId{nullptr};
    int *mDestinationBuffer{nullptr};
};

#endif //SAMPLES_DSPJNIPUSHER_H
