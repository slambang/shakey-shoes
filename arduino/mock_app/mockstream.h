#ifndef APP_MockStream_H
#define APP_MockStream_H

#include <data_stream.h>

#include <thread>
#include <mutex>
#include <queue>

class MockStream;

class MockStreamListener {
public:
    virtual ~MockStreamListener() = default;

    virtual void onWrite(BYTE data, MockStream *stream) = 0;
};

class MockStream : public DataStream {
public:
    explicit MockStream(MockStreamListener *l) : listener(l) {}

    ~MockStream() override {
        alive = false;
        if (thread) thread->join();
        delete thread;
        delete listener;
    }

    void start() {
        thread = new std::thread(&MockStream::threadMethod, this);
    }

    bool available() override {
        std::lock_guard<std::mutex> lock(bufferMutex);
        return !readBuffer.empty();
    }

    int read() override {
        std::lock_guard<std::mutex> lock(bufferMutex);
        BYTE value = readBuffer.front();
        readBuffer.pop();
        return value;
    }

    void write(int data) override {
        std::lock_guard<std::mutex> lock(bufferMutex);
        writeBuffer.push(data);
    }

    void put(BYTE data) {
        std::lock_guard<std::mutex> lock(bufferMutex);
        readBuffer.push(data);
    }

private:
    void threadMethod() {
        alive = true;
        while (alive) {
            if (!writeBuffer.empty() && bufferMutex.try_lock()) {
                BYTE data = writeBuffer.front();
                writeBuffer.pop();
                bufferMutex.unlock();
                listener->onWrite(data, this);
            }
        }
    }

    std::queue<BYTE> readBuffer;
    std::queue<BYTE> writeBuffer;

    std::mutex bufferMutex;
    volatile bool alive{true};
    std::thread *thread{nullptr};
    MockStreamListener *listener{nullptr};
};

#endif //APP_MockStream_H
