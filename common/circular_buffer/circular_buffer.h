/******************************************************************************
                  Copyright (c) 2018 EmbedJournal
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
    Author : Siddharth Chandrasekaran
    Email  : siddharth@embedjournal.com
    Date   : Sun Aug  5 09:42:31 IST 2018
******************************************************************************/
#ifndef __CIRCULAR_BYTE_BUFFER_H_
#define __CIRCULAR_BYTE_BUFFER_H_

// Adapted from: https://github.com/EmbedJournal/c-utils/blob/master/archive/circular-byte-buffer.h

#include <stdint.h>

struct circ_bbuf_t {

    circ_bbuf_t(const int maxlen): buffer(new uint8_t[maxlen + 1]), maxlen(maxlen + 1) {}

    ~circ_bbuf_t() {
        delete[]buffer;
    }

    void reset() {
        head = 0;
        tail = 0;
    }

    uint8_t *const buffer;
    int head{0};
    int tail{0};
    const int maxlen;
};

#define CIRC_BBUF_DEF(x, y)                \
    uint8_t x##_data_space[y+1];          \
    circ_bbuf_t x = {                     \
        .buffer = x##_data_space,         \
        .head = 0,                        \
        .tail = 0,                        \
        .maxlen = y+1                     \
    }

int circ_bbuf_push(circ_bbuf_t *c, uint8_t data) {
    int next;

    next = c->head + 1;  // next is where head will point to after this write.
    if (next >= c->maxlen)
        next = 0;

    // if the head + 1 == tail, circular buffer is full. Notice that one slot
    // is always left empty to differentiate empty vs full condition
    if (next == c->tail)
        return -1;

    c->buffer[c->head] = data;  // Load data and then move
    c->head = next;             // head to next data offset.
    return 0;  // return success to indicate successful push.
}

int circ_bbuf_pop(circ_bbuf_t *c, uint8_t *data) {
    int next;

    if (c->head == c->tail)  // if the head == tail, we don't have any data
        return -1;

    next = c->tail + 1;  // next is where tail will point to after this read.
    if (next >= c->maxlen)
        next = 0;

    *data = c->buffer[c->tail];  // Read data and then move
    c->tail = next;              // tail to next offset.
    return 0;  // return success to indicate successful push.
}

int circ_bbuf_free_space(circ_bbuf_t *c) {
    int freeSpace;
    freeSpace = c->tail - c->head;
    if (freeSpace <= 0)
        freeSpace += c->maxlen;
    return freeSpace - 1; // -1 to account for the always-empty slot.
}

#endif // __CIRCULAR_BYTE_BUFFER_H_
