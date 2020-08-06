cmake_minimum_required(VERSION 3.15)
project(mock_app)

set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++14 -pthread")

include_directories("../../common/circular_buffer")

add_executable(mock_app
        main.cpp
        mockplatform.h
        mockstream.h)
