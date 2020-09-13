cmake_minimum_required(VERSION 3.4.1)

# Set the name of the project and store it in PROJECT_NAME. Also set the following variables:
# PROJECT_SOURCE_DIR (usually the root directory where Oboe has been cloned e.g.)
# PROJECT_BINARY_DIR (usually the containing project's binary directory,
# e.g. ${OBOE_HOME}/samples/RhythmGame/.externalNativeBuild/cmake/ndkExtractorDebug/x86/oboe-bin)
project(oboe)

message(${CMAKE_CURRENT_SOURCE_DIR})

set (oboe_sources
    ${OBOE_DIR}/src/aaudio/AAudioLoader.cpp
    ${OBOE_DIR}/src/aaudio/AudioStreamAAudio.cpp
    ${OBOE_DIR}/src/common/AudioSourceCaller.cpp
    ${OBOE_DIR}/src/common/AudioStream.cpp
    ${OBOE_DIR}/src/common/AudioStreamBuilder.cpp
    ${OBOE_DIR}/src/common/DataConversionFlowGraph.cpp
    ${OBOE_DIR}/src/common/FilterAudioStream.cpp
    ${OBOE_DIR}/src/common/FixedBlockAdapter.cpp
    ${OBOE_DIR}/src/common/FixedBlockReader.cpp
    ${OBOE_DIR}/src/common/FixedBlockWriter.cpp
    ${OBOE_DIR}/src/common/LatencyTuner.cpp
    ${OBOE_DIR}/src/common/SourceFloatCaller.cpp
    ${OBOE_DIR}/src/common/SourceI16Caller.cpp
    ${OBOE_DIR}/src/common/Utilities.cpp
    ${OBOE_DIR}/src/common/QuirksManager.cpp
    ${OBOE_DIR}/src/fifo/FifoBuffer.cpp
    ${OBOE_DIR}/src/fifo/FifoController.cpp
    ${OBOE_DIR}/src/fifo/FifoControllerBase.cpp
    ${OBOE_DIR}/src/fifo/FifoControllerIndirect.cpp
    ${OBOE_DIR}/src/flowgraph/FlowGraphNode.cpp
    ${OBOE_DIR}/src/flowgraph/ChannelCountConverter.cpp
    ${OBOE_DIR}/src/flowgraph/ClipToRange.cpp
    ${OBOE_DIR}/src/flowgraph/ManyToMultiConverter.cpp
    ${OBOE_DIR}/src/flowgraph/MonoToMultiConverter.cpp
    ${OBOE_DIR}/src/flowgraph/MultiToMonoConverter.cpp
    ${OBOE_DIR}/src/flowgraph/RampLinear.cpp
    ${OBOE_DIR}/src/flowgraph/SampleRateConverter.cpp
    ${OBOE_DIR}/src/flowgraph/SinkFloat.cpp
    ${OBOE_DIR}/src/flowgraph/SinkI16.cpp
    ${OBOE_DIR}/src/flowgraph/SinkI24.cpp
    ${OBOE_DIR}/src/flowgraph/SourceFloat.cpp
    ${OBOE_DIR}/src/flowgraph/SourceI16.cpp
    ${OBOE_DIR}/src/flowgraph/SourceI24.cpp
    ${OBOE_DIR}/src/flowgraph/resampler/IntegerRatio.cpp
    ${OBOE_DIR}/src/flowgraph/resampler/LinearResampler.cpp
    ${OBOE_DIR}/src/flowgraph/resampler/MultiChannelResampler.cpp
    ${OBOE_DIR}/src/flowgraph/resampler/PolyphaseResampler.cpp
    ${OBOE_DIR}/src/flowgraph/resampler/PolyphaseResamplerMono.cpp
    ${OBOE_DIR}/src/flowgraph/resampler/PolyphaseResamplerStereo.cpp
    ${OBOE_DIR}/src/flowgraph/resampler/SincResampler.cpp
    ${OBOE_DIR}/src/flowgraph/resampler/SincResamplerStereo.cpp
    ${OBOE_DIR}/src/opensles/AudioInputStreamOpenSLES.cpp
    ${OBOE_DIR}/src/opensles/AudioOutputStreamOpenSLES.cpp
    ${OBOE_DIR}/src/opensles/AudioStreamBuffered.cpp
    ${OBOE_DIR}/src/opensles/AudioStreamOpenSLES.cpp
    ${OBOE_DIR}/src/opensles/EngineOpenSLES.cpp
    ${OBOE_DIR}/src/opensles/OpenSLESUtilities.cpp
    ${OBOE_DIR}/src/opensles/OutputMixerOpenSLES.cpp
    ${OBOE_DIR}/src/common/StabilizedCallback.cpp
    ${OBOE_DIR}/src/common/Trace.cpp
    ${OBOE_DIR}/src/common/Version.cpp
    )

add_library(oboe ${oboe_sources})

# Specify directories which the compiler should look for headers
target_include_directories(oboe
        PRIVATE src
        PUBLIC include)

# Compile Flags:
#     Enable -Werror when building debug config
#     Enable -Ofast
target_compile_options(oboe
        PRIVATE
        -std=c++14
        -Wall
        -Wextra-semi
        -Wshadow
        -Wshadow-field
        -Ofast
        "$<$<CONFIG:DEBUG>:-Werror>")

# Enable logging of D,V for debug builds
target_compile_definitions(oboe PUBLIC $<$<CONFIG:DEBUG>:OBOE_ENABLE_LOGGING=1>)

target_link_libraries(oboe PRIVATE log OpenSLES)

# When installing oboe put the libraries in the lib/<ABI> folder e.g. lib/arm64-v8a
install(TARGETS oboe
        LIBRARY DESTINATION lib/${ANDROID_ABI}
        ARCHIVE DESTINATION lib/${ANDROID_ABI})

# Also install the headers
install(DIRECTORY include/oboe DESTINATION include)
