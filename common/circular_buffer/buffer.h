#ifndef CIRCULARBUFFER_CIRCULARBUFFER_H
#define CIRCULARBUFFER_CIRCULARBUFFER_H

#include "platform.h"
#include "circular_buffer.h"

class CircularBuffer {
public:

    virtual ~CircularBuffer() = default;

    virtual void initialise(int size) = 0;

    virtual void destroy() = 0;

    virtual bool read(BYTE &ref) = 0;

    virtual bool write(BYTE data) = 0;

    virtual void reset() = 0;

    virtual bool isFull() = 0;
};

class CircularBufferImpl : public CircularBuffer {
public:
    ~CircularBufferImpl() override {
        deallocate();
    }

    void initialise(int size) override {
        buffer = new circ_bbuf_t(size);
    }

    void destroy() override {
        deallocate();
    }

    bool read(BYTE &ref) override {
        return !circ_bbuf_pop(buffer, &ref);
    }

    bool write(const BYTE data) override {
        return circ_bbuf_push(buffer, data);
    }

    void reset() override {
        if (buffer != nullptr) {
            buffer->reset();
        }
    }

    bool isFull() override {
        return buffer->head + 1 == buffer->maxlen;
    }

private:
    void deallocate() {
        if (buffer != nullptr) {
            delete buffer;
            buffer = nullptr;
        }
    }

    circ_bbuf_t *buffer{nullptr};
};

#endif //CIRCULARBUFFER_CIRCULARBUFFER_H
