#ifndef DESKTOP_DEBUGLOGGER_H
#define DESKTOP_DEBUGLOGGER_H

#ifdef BEATPROCESSOR_DEBUG

#include <string>
#include <iomanip>

#include <write/AudioFrameWriter.h>
#include <write/EventWriter.h>
#include <write/StringWriter.h>
#include <write/MagnitudesWriter.h>
#include <write/AverageMagnitudeWriter.h>
#include <BeatProcessor.h>

using namespace std;

class DebugLogger {
public:
    static const int SUCCESS = 0;

    DebugLogger(const char *inputFile, const char *debugOutputBase, BeatProcessorConfig *config) {

        this->channels = config->channels;
        this->totalFrames = config->totalFrames;

        logWriter = new StringWriter;
        eventWriter = new EventWriter(config->frameBufferSize, config->threshold);
        magnitudesWriter = new MagnitudesWriter(config->sampleRate, config->frameBufferSize);
        averageMagnitudeWriter = new AverageMagnitudeWriter(config->frameBufferSize);

        string signalFile = string(debugOutputBase) + "/mono";
        string magnitudesFile = string(debugOutputBase) + "/magnitude";
        string gistFile = string(debugOutputBase) + "/event";
        string filteredOutput = string(debugOutputBase) + "/filtered";
        string logOutput = string(debugOutputBase) + "/log";
        string averageMagnitudeFile = string(debugOutputBase) + "/average_magnitude";

        if (!filteredWriter.open(filteredOutput.c_str(), totalFrames)) {
            status = -1;
            return;
        }

        if (!audioFrameWriter.open(signalFile.c_str(), totalFrames)) {
            status = -2;
            return;
        }

        if (!eventWriter->open(gistFile.c_str())) {
            status = -3;
            return;
        }

        if (!magnitudesWriter->open(magnitudesFile.c_str())) {
            status = -4;
            return;
        }

        if (!logWriter->open(logOutput.c_str())) {
            status = -5;
            return;
        }

        if (!averageMagnitudeWriter->open(averageMagnitudeFile.c_str())) {
            status = -6;
            return;
        }

        logWriter->write(inputFile);
        logWriter->write("\n");
    }

    ~DebugLogger() {
        delete logWriter;
        delete eventWriter;
        delete magnitudesWriter;
    }

    void addResult(float *window, BeatProcessorLog *log) {

        eventWriter->write(log->event);
        filteredWriter.write(log->filterOutput, 1, log->numFrames);
        audioFrameWriter.write(log->monoWindow, 1, log->numFrames);
        magnitudesWriter->write(log->filterMagnitudes, 1, log->numFrames / 2);
        averageMagnitudeWriter->write(log->averageMagnitude);

        frameCount += log->numFrames;
        totalFilterTime += log->filterTime;
        totalEventTime += log->eventTime;
        totalAverageMagnitudeTime += log->averageMagnitudeTime;
        totalMonoTime += log->monoTime;
    }

    void onFinish(BeatProcessorConfig *config, BeatProcessorLog *log, int64_t totalProcessorTime, int64_t numReads, int64_t totalReadTime) {

        float averageFrameTime = (float)totalProcessorTime / (float)frameCount;
        float averageReadTime = (float)totalReadTime / (float)numReads;
        float averageMonoTime = (float)totalMonoTime / (float)frameCount;
        float averageFilterTime = (float)totalFilterTime / (float)frameCount;
        float averageEventTime = (float)totalEventTime / (float)frameCount;
        float averageAverageMagnitudeTime = (float)totalAverageMagnitudeTime / (float)frameCount;

        std::stringstream buffer;

        buffer<<fixed<<setprecision(10);
        buffer<<"Channels: "<<config->channels<<endl;
        buffer<<"Sample rate: "<<config->sampleRate<<"hz"<<endl;
        buffer<<"Frame buffer size: "<<config->frameBufferSize<<" ("<< config->frameBufferSize / (config->sampleRate/1000)<<"ms window)"<<endl;
        buffer<<"Onset threshold: " << config->threshold <<endl;
        buffer<<"Filter center Hz: "<<config->filterCenterHz<<"hz"<<endl;
        buffer<<"Filter width Hz: "<<config->filterWidthHz<<"hz"<<endl;
        buffer<<"Total frames: "<<totalFrames <<endl;
        printFileLength(buffer, (totalFrames/config->sampleRate) * 1000);

        buffer<<endl;
        buffer<<"1: Mix down to mono signal"<<endl;
        buffer<<"\tTotal: " << totalMonoTime <<"ms"<<endl;
        buffer<<"\tAverage per frame: " << averageMonoTime <<"ms"<<endl;

        buffer<<endl;
        buffer<<"2: Filter of mono signal ("<<config->filterCenterHz<<"hz center, "<<config->filterWidthHz<<"hz wide)" <<endl;
        buffer<<"\tTotal: "<<totalFilterTime<<"ms"<<endl;
        buffer<<"\tAverage per frame: "<<averageFilterTime<<"ms"<<endl;

        buffer<<endl;
        buffer<<"3: Onset detection of filtered signal (" << config->threshold <<" threshold)" <<endl;
        buffer<<"\tTotal: "<<totalEventTime<<"ms"<<endl;
        buffer<<"\tAverage per frame: "<<averageEventTime<<"ms"<<endl;

        buffer<<endl;
        buffer<<"4: Average magnitudes of filtered signal" <<endl;
        buffer<<"\tTotal: "<<totalAverageMagnitudeTime<<"ms"<<endl;
        buffer<<"\tAverage per frame: "<<averageAverageMagnitudeTime<<"ms"<<endl;

        buffer<<endl;
        buffer<<"Totals"<<endl;
        buffer<<"\tProcessor time: "<<totalProcessorTime<<"ms"<<endl;
        buffer<<"\tProcessed frames: "<<frameCount<<" (/"<<config->frameBufferSize<<" = "<<(frameCount / config->frameBufferSize)<<" iterations)"<<endl;
        buffer<<"\tAverage frame time: "<<averageFrameTime<<"ms"<<endl;
        buffer<<endl;
        buffer<<"\tRead time: "<<totalReadTime<<"ms"<<endl;
        buffer<<"\tNumber of reads: "<<numReads <<endl;
        buffer<<"\tAverage read time: "<<averageReadTime<<"ms"<<endl;

        logWriter->write(buffer.str().c_str());
    }

    int getStatus() {return status;}
private:

    static void printFileLength(std::stringstream &buffer, long milli) {

        long hr = milli / 3600000;
        milli = milli - 3600000 * hr;

        long min = milli / 60000;
        milli = milli - 60000 * min;

        long sec = milli / 1000;
        milli = milli - 1000 * sec;

        buffer << "File time: " << hr << ":" << min << ":" << sec << ":" << milli << endl;
    }

    int status { 0 };
    int totalFrames { 0 };
    int channels { 0 };

    int64_t frameCount { 0 };
    int64_t totalFilterTime { 0 };
    int64_t totalEventTime { 0 };
    int64_t totalAverageMagnitudeTime { 0 };
    int64_t totalMonoTime { 0 };

    AudioFrameWriter filteredWriter;
    AudioFrameWriter audioFrameWriter;
    EventWriter *eventWriter { nullptr };
    MagnitudesWriter *magnitudesWriter { nullptr };
    StringWriter *logWriter { nullptr };
    AverageMagnitudeWriter* averageMagnitudeWriter { nullptr };
};

#endif // BEATPROCESSOR_DEBUG

#endif //DESKTOP_DEBUGLOGGER_H
