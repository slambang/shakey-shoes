#include "appfactory.h"
#include "mockstream.h"
#include "mockplatform.h"

class MockStreamListenerImpl : public MockStreamListener {
public:
    static void refill(MockStream *stream, int refills, unsigned int refillSize) {
        for (int i = 0; i < refills; ++i) {
            for (int j = 0; j < refillSize; ++j) {
                std::cout << "Refilling: " << 0 << std::endl;
                writeByte(stream, 6, 4);
            }
        }
    }

    MockStreamListenerImpl(int numRefills, int windowSizeMs, int refillSize, int maxOverflows) : numRefills(numRefills),
                                                                                                 windowSizeMs(
                                                                                                         windowSizeMs),
                                                                                                 refillSize(refillSize),
                                                                                                 maxOverflows(
                                                                                                         maxOverflows) {}

    void onWrite(BYTE data, MockStream *stream) override {

        if (freeRam == -1) {
            buffer[bufferSize++] = data;
            if (bufferSize == 4) {
                freeRam = DataStream::toInt(buffer);
                std::cout << "Free RAM: " << freeRam << std::endl;
                sendConfig(stream);
            }
        } else {
            if (data == ClientImpl::SIGNAL_OUT_READY) {
                std::cout << "SIGNAL_OUT_READY" << std::endl;

                MockStreamListenerImpl::refill(stream, 4, 3);

                writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND, 0);
                writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND_RESUME, 0);
            } else if (data == ClientImpl::SIGNAL_OUT_REQUEST_REFILL) {
                std::cout << "SIGNAL_OUT_REQUEST_REFILL" << std::endl;
                MockStreamListenerImpl::refill(stream, 1, refillSize);
            }
        }
    }

private:
    static void writeByte(MockStream *stream, BYTE data, long delayMs) {
        if (delayMs > 0) {
            std::this_thread::sleep_for(std::chrono::milliseconds(delayMs));
        }
        stream->put(data);
    }

    void sendConfig(MockStream *stream) {

        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND, 0);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND_CONFIG, 0);
        writeByte(stream, numRefills, 20);

        DataStream::toByteArray(refillSize, buffer);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND, 10);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND_CONFIG, 0);
        writeByte(stream, buffer[0], 39);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND, 0);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND_CONFIG, 0);
        writeByte(stream, buffer[1], 2);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND, 0);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND_CONFIG, 0);
        writeByte(stream, buffer[2], 0);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND, 0);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND_CONFIG, 0);
        writeByte(stream, buffer[3], 101);

        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND, 0);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND_CONFIG, 0);
        writeByte(stream, windowSizeMs, 1000);

        DataStream::toByteArray(maxOverflows, buffer);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND, 0);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND_CONFIG, 0);
        writeByte(stream, buffer[0], 326);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND, 0);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND_CONFIG, 0);
        writeByte(stream, buffer[1], 20);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND, 0);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND_CONFIG, 0);
        writeByte(stream, buffer[2], 1);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND, 0);
        writeByte(stream, ClientImpl::SIGNAL_IN_COMMAND_CONFIG, 0);
        writeByte(stream, buffer[3], 756);

        std::cout << "Config sent" << std::endl;
    }

    int freeRam{-1};

    int numRefills;
    int windowSizeMs;
    int maxOverflows;
    unsigned int refillSize{0};

    BYTE buffer[1024]{0};
    int bufferSize{0};
};

const int MAX_LOOPS = 10000000;

int main() {

    auto listener = new MockStreamListenerImpl(4, 10000, 3, 50);
    auto mockClient = new MockStream(listener);
    mockClient->start();
    mockClient->put(ClientImpl::SIGNAL_IN_COMMAND);
    mockClient->put(ClientImpl::SIGNAL_IN_COMMAND_CONNECT);

    mockClient->put(ClientImpl::SIGNAL_IN_COMMAND);
    mockClient->put(ClientImpl::SIGNAL_IN_COMMAND_RESET);

    MockPlatform platform;
    App *app = newAppInstance(mockClient, &platform);

    int loopCount = 0;
    while (loopCount++ < MAX_LOOPS) app->loop();

    mockClient->put(ClientImpl::SIGNAL_IN_COMMAND);
    mockClient->put(ClientImpl::SIGNAL_IN_COMMAND_PAUSE);

    mockClient->put(ClientImpl::SIGNAL_IN_COMMAND);
    mockClient->put(ClientImpl::SIGNAL_IN_COMMAND_RESET);
    MockStreamListenerImpl::refill(mockClient, 4, 3);

    mockClient->put(ClientImpl::SIGNAL_IN_COMMAND);
    mockClient->put(ClientImpl::SIGNAL_IN_COMMAND_RESUME);

    loopCount = 0;
    while (loopCount++ < MAX_LOOPS) app->loop();

    delete app;
    return 0;
}
