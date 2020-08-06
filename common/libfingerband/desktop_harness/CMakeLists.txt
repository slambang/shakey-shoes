cmake_minimum_required(VERSION 3.4.1)

add_definitions(-DBEATPROCESSOR_DEBUG)

set(FINGERBAND_DIR_RELATIVE ${PROJECT_SOURCE_DIR}/../libfingerband)
message(${FINGERBAND_DIR_RELATIVE})

get_filename_component(FINGERBAND_DIR
                       "${FINGERBAND_DIR_RELATIVE}"
                       ABSOLUTE)
message(${FINGERBAND_DIR})

# -------------------------  Dependencies START

# ----- IIR1
set(IIR1_DIR ${FINGERBAND_DIR}/libs/iir1)
include_directories("${IIR1_DIR}")
include_directories("${IIR1_DIR}/iir")
add_library(iir1
            SHARED
            ${IIR1_DIR}/iir/Biquad.cpp
            ${IIR1_DIR}/iir/Butterworth.cpp
            ${IIR1_DIR}/iir/Cascade.cpp
            ${IIR1_DIR}/iir/ChebyshevI.cpp
            ${IIR1_DIR}/iir/ChebyshevII.cpp
            ${IIR1_DIR}/iir/Custom.cpp
            ${IIR1_DIR}/iir/PoleFilter.cpp
            ${IIR1_DIR}/iir/RBJ.cpp
            ${IIR1_DIR}/iir/State.cpp)

# ----- Gist
set(GIST_DIR ${FINGERBAND_DIR}/libs/Gist/src)
include_directories(${GIST_DIR})
add_library(gist
            SHARED
            ${GIST_DIR}/Gist.cpp
            ${GIST_DIR}/core/CoreFrequencyDomainFeatures.cpp
            ${GIST_DIR}/core/CoreTimeDomainFeatures.cpp
            ${GIST_DIR}/fft/WindowFunctions.cpp
            ${GIST_DIR}/mfcc/MFCC.cpp
            ${GIST_DIR}/onset-detection-functions/OnsetDetectionFunction.cpp
            ${GIST_DIR}/pitch/Yin.cpp)

# ----- ofxGist
set(OFXGIST_DIR ${FINGERBAND_DIR}/libs/ofxGist)
include_directories("${OFXGIST_DIR}/src")
add_library(ofxgist
            SHARED
            ${OFXGIST_DIR}/src/ofxGist.cpp)

# ----- SndFile
set(SNDFILE_DIR ${FINGERBAND_DIR}/libs/libsndfile)
include_directories("${SNDFILE_DIR}/src")
add_library(sndfile
            SHARED
            ${SNDFILE_DIR}/src/common.c
            ${SNDFILE_DIR}/src/file_io.c
            ${SNDFILE_DIR}/src/command.c
            ${SNDFILE_DIR}/src/pcm.c
            ${SNDFILE_DIR}/src/ulaw.c
            ${SNDFILE_DIR}/src/alaw.c
            ${SNDFILE_DIR}/src/float32.c
            ${SNDFILE_DIR}/src/double64.c
            ${SNDFILE_DIR}/src/ima_adpcm.c
            ${SNDFILE_DIR}/src/ms_adpcm.c
            ${SNDFILE_DIR}/src/gsm610.c
            ${SNDFILE_DIR}/src/dwvw.c
            ${SNDFILE_DIR}/src/vox_adpcm.c
            ${SNDFILE_DIR}/src/interleave.c
            ${SNDFILE_DIR}/src/strings.c
            ${SNDFILE_DIR}/src/dither.c
            ${SNDFILE_DIR}/src/broadcast.c
            ${SNDFILE_DIR}/src/audio_detect.c
            ${SNDFILE_DIR}/src/ima_oki_adpcm.c
            ${SNDFILE_DIR}/src/ima_oki_adpcm.h
            ${SNDFILE_DIR}/src/chunk.c
            ${SNDFILE_DIR}/src/ogg.c
            ${SNDFILE_DIR}/src/chanmap.c
            ${SNDFILE_DIR}/src/windows.c
            ${SNDFILE_DIR}/src/id3.c
            ${SNDFILE_DIR}/src/sndfile.c
            ${SNDFILE_DIR}/src/aiff.c
            ${SNDFILE_DIR}/src/au.c
            ${SNDFILE_DIR}/src/avr.c
            ${SNDFILE_DIR}/src/caf.c
            ${SNDFILE_DIR}/src/dwd.c
            ${SNDFILE_DIR}/src/flac.c
            ${SNDFILE_DIR}/src/g72x.c
            ${SNDFILE_DIR}/src/htk.c
            ${SNDFILE_DIR}/src/ircam.c
            ${SNDFILE_DIR}/src/macbinary3.c
            ${SNDFILE_DIR}/src/macos.c
            ${SNDFILE_DIR}/src/mat4.c
            ${SNDFILE_DIR}/src/mat5.c
            ${SNDFILE_DIR}/src/nist.c
            ${SNDFILE_DIR}/src/paf.c
            ${SNDFILE_DIR}/src/pvf.c
            ${SNDFILE_DIR}/src/raw.c
            ${SNDFILE_DIR}/src/rx2.c
            ${SNDFILE_DIR}/src/sd2.c
            ${SNDFILE_DIR}/src/sds.c
            ${SNDFILE_DIR}/src/svx.c
            ${SNDFILE_DIR}/src/txw.c
            ${SNDFILE_DIR}/src/voc.c
            ${SNDFILE_DIR}/src/wve.c
            ${SNDFILE_DIR}/src/w64.c
            ${SNDFILE_DIR}/src/wav_w64.c
            ${SNDFILE_DIR}/src/wav.c
            ${SNDFILE_DIR}/src/xi.c
            ${SNDFILE_DIR}/src/mpc2k.c
            ${SNDFILE_DIR}/src/rf64.c
            ${SNDFILE_DIR}/src/GSM610/add.c
            ${SNDFILE_DIR}/src/GSM610/code.c
            ${SNDFILE_DIR}/src/GSM610/decode.c
            ${SNDFILE_DIR}/src/GSM610/gsm_create.c
            ${SNDFILE_DIR}/src/GSM610/gsm_decode.c
            ${SNDFILE_DIR}/src/GSM610/gsm_destroy.c
            ${SNDFILE_DIR}/src/GSM610/gsm_encode.c
            ${SNDFILE_DIR}/src/GSM610/gsm_option.c
            ${SNDFILE_DIR}/src/GSM610/long_term.c
            ${SNDFILE_DIR}/src/GSM610/lpc.c
            ${SNDFILE_DIR}/src/GSM610/preprocess.c
            ${SNDFILE_DIR}/src/GSM610/rpe.c
            ${SNDFILE_DIR}/src/GSM610/short_term.c
            ${SNDFILE_DIR}/src/GSM610/table.c
            ${SNDFILE_DIR}/src/G72x/g721.c
            ${SNDFILE_DIR}/src/G72x/g723_16.c
            ${SNDFILE_DIR}/src/G72x/g723_24.c
            ${SNDFILE_DIR}/src/G72x/g723_40.c
            ${SNDFILE_DIR}/src/G72x/g72x.c)

# ----- KissFFT
set(KISS_DIR ${FINGERBAND_DIR}/libs/kissfft)
include_directories(${KISS_DIR})
add_library(kissfft
            SHARED
            ${KISS_DIR}/kiss_fft.c)

# -------------------------  Dependencies END

target_link_libraries(gist kissfft)
target_link_libraries(ofxgist gist)

add_definitions(-DUSE_KISS_FFT)
#add_definitions(-DFIXED_POINT=16)
#target_compile_options(fingerband PRIVATE -std=c++14 -Wall -Werror "$<$<CONFIG:RELEASE>:-Ofast>")

include_directories(${FINGERBAND_DIR}/src)

# TODO: Separate main.cpp and the rest (new library)
set(SOURCE_FILES
    ${PROJECT_SOURCE_DIR}/../fingerband_runner.cpp
    ${FINGERBAND_DIR}/src/dsp/DspUtil.cpp
    ${FINGERBAND_DIR}/src/dsp/BeatGeneratorImpl.cpp
    ${FINGERBAND_DIR}/src/write/MagnitudesWriter.cpp
    ${FINGERBAND_DIR}/src/write/EventWriter.cpp
    ${FINGERBAND_DIR}/src/write/StringWriter.cpp
    ${FINGERBAND_DIR}/src/write/AudioFrameWriter.cpp
    ${FINGERBAND_DIR}/src/write/AverageMagnitudeWriter.cpp
    ${FINGERBAND_DIR}/src/decode/WavFileDataSource.cpp
    ${FINGERBAND_DIR}/src/BeatProcessor.cpp
    ${FINGERBAND_DIR}/src/PreProcessor.cpp
    ${FINGERBAND_DIR}/src/DebugLogger.h)

add_executable(fingerband
               ${SOURCE_FILES})

target_link_libraries(fingerband sndfile ofxgist iir1)
