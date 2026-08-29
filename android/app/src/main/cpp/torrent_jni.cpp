#include <jni.h>
#include <android/log.h>
#include <mutex>
#include <string>

#define LOG_TAG "SceneBoxTorrent"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

namespace {
std::mutex g_mutex;
std::string g_magnet;
bool g_active = false;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_scenebox_NativeTorrentBridge_nativeStart(
    JNIEnv* env, jobject /* thiz */, jstring magnet) {
    if (magnet == nullptr) return JNI_FALSE;

    const char* raw = env->GetStringUTFChars(magnet, nullptr);
    if (raw == nullptr) return JNI_FALSE;

    {
        std::lock_guard<std::mutex> lock(g_mutex);
        g_magnet = raw;
        g_active = true;
    }

    env->ReleaseStringUTFChars(magnet, raw);
    LOGI("Torrent start requested");
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_scenebox_NativeTorrentBridge_nativeStop(
    JNIEnv* /* env */, jobject /* thiz */) {
    std::lock_guard<std::mutex> lock(g_mutex);
    g_magnet.clear();
    g_active = false;
    LOGI("Torrent stopped");
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_scenebox_NativeTorrentBridge_nativeIsActive(
    JNIEnv* /* env */, jobject /* thiz */) {
    std::lock_guard<std::mutex> lock(g_mutex);
    return g_active ? JNI_TRUE : JNI_FALSE;
}
