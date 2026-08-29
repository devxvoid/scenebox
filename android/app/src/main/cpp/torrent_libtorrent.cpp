#include <jni.h>
#include <mutex>
#include <string>
#include <vector>

#if __has_include(<libtorrent/session.hpp>) && __has_include(<libtorrent/magnet_uri.hpp>)
#define SCENEBOX_HAS_LIBTORRENT 1
#include <libtorrent/add_torrent_params.hpp>
#include <libtorrent/magnet_uri.hpp>
#include <libtorrent/session.hpp>
#include <libtorrent/torrent_handle.hpp>
#include <libtorrent/torrent_status.hpp>
#else
#define SCENEBOX_HAS_LIBTORRENT 0
#endif

namespace {
std::mutex g_mutex;
#if SCENEBOX_HAS_LIBTORRENT
std::unique_ptr<lt::session> g_session;
lt::torrent_handle g_handle;
#endif
bool g_active = false;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_scenebox_NativeTorrentBridge_nativeStart(JNIEnv* env, jobject, jstring magnet) {
    if (!magnet) return JNI_FALSE;
    const char* raw = env->GetStringUTFChars(magnet, nullptr);
    if (!raw) return JNI_FALSE;

    std::lock_guard<std::mutex> lock(g_mutex);
#if SCENEBOX_HAS_LIBTORRENT
    try {
        lt::add_torrent_params params = lt::parse_magnet_uri(raw);
        params.save_path = "/data/data/com.scenebox/files/torrents";
        g_session = std::make_unique<lt::session>();
        g_handle = g_session->add_torrent(params);
        g_active = g_handle.is_valid();
    } catch (...) {
        g_active = false;
    }
#else
    g_active = false;
#endif

    env->ReleaseStringUTFChars(magnet, raw);
    return g_active ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_scenebox_NativeTorrentBridge_nativeStop(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(g_mutex);
#if SCENEBOX_HAS_LIBTORRENT
    if (g_handle.is_valid()) g_handle.pause();
    g_handle = {};
    g_session.reset();
#endif
    g_active = false;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_scenebox_NativeTorrentBridge_nativeIsActive(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(g_mutex);
    return g_active ? JNI_TRUE : JNI_FALSE;
}
