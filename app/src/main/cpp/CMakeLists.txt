cmake_minimum_required(VERSION 3.22.1)

project("edge_detector")

# OpenCV package
find_package(OpenCV REQUIRED)

# Add library
add_library(edge_detector SHARED
    ../../../jni/edge_detector.cpp)

# Include OpenCV headers
target_include_directories(edge_detector PRIVATE ${OpenCV_INCLUDE_DIRS})

# Link libraries
target_link_libraries(edge_detector
    ${OpenCV_LIBS}
    android
    log)