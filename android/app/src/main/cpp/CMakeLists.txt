cmake_minimum_required(VERSION 3.22.1)
project(scenebox_torrent)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(scenebox_torrent SHARED torrent_jni.cpp)

find_library(log-lib log)

target_link_libraries(scenebox_torrent ${log-lib})

# libtorrent is intentionally provided as an Android-native dependency rather
# than reusing the iOS XCFramework contained in the source repository.
# Wire the prebuilt/static libtorrent target here when the Android build of
# libtorrent is added for each ABI.
