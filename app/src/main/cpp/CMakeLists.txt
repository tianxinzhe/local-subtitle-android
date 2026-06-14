cmake_minimum_required(VERSION 3.22.1)
project("lemonsubtitle")

add_library(lemonsubtitle SHARED
    whisper_bridge.cpp
    ffmpeg_bridge.cpp
)

# Find whisper.cpp - users should clone whisper.cpp into app/src/main/cpp/
if(EXISTS ${CMAKE_CURRENT_SOURCE_DIR}/whisper.cpp)
    set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
    add_subdirectory(whisper.cpp)
    target_link_libraries(lemonsubtitle whisper)
    target_include_directories(lemonsubtitle PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/whisper.cpp/include)
    target_compile_definitions(lemonsubtitle PRIVATE WHISPER_AVAILABLE)
endif()

# Find ffmpeg-android
if(EXISTS ${CMAKE_CURRENT_SOURCE_DIR}/ffmpeg-android/lib/${ANDROID_ABI})
    set(FFMPEG_LIB_DIR ${CMAKE_CURRENT_SOURCE_DIR}/ffmpeg-android/lib/${ANDROID_ABI})
    set(FFMPEG_INCLUDE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/ffmpeg-android/include)
    target_include_directories(lemonsubtitle PRIVATE ${FFMPEG_INCLUDE_DIR})
    target_link_directories(lemonsubtitle PRIVATE ${FFMPEG_LIB_DIR})
    target_link_libraries(lemonsubtitle
        avformat avcodec avutil swresample
    )
endif()

target_include_directories(lemonsubtitle PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})

# Required for Android log
target_link_libraries(lemonsubtitle log)

# C++ standard
set_target_properties(lemonsubtitle PROPERTIES
    CXX_STANDARD 17
    CXX_STANDARD_REQUIRED ON
)
