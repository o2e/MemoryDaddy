#include <jni.h>
#include <string>

#include "MemoryTools.h"
#include "JniHelper.h"

extern "C"
JNIEXPORT void JNICALL
Java_moe_ore_servlet_ess_MemoryToolsNative_killGG(JNIEnv *env, jclass clazz) {
    killGG();
}
extern "C"
JNIEXPORT void JNICALL
Java_moe_ore_memory_SoftApplication_initNative(JNIEnv *env, jobject thiz) {

}
extern "C"
JNIEXPORT void JNICALL
Java_moe_ore_servlet_ess_MemoryToolsNative_killXs(JNIEnv *env, jclass clazz) {
    killXs();
}

extern "C"
JNIEXPORT void JNICALL
Java_moe_ore_servlet_ess_MemoryToolsNative_init(JNIEnv *env, jclass clazz) {

}

extern "C"
JNIEXPORT jint JNICALL
Java_moe_ore_servlet_ess_MemoryToolsNative_getPid(JNIEnv *env, jclass clazz, jstring package_name) {
    int pid = getPID(jstringToString(env, package_name));
    return (jint) pid;
}
extern "C"
JNIEXPORT jchar JNICALL
Java_moe_ore_servlet_ess_MemoryToolsNative_getProcessState(JNIEnv *env, jclass clazz,
                                                           jstring package_name) {
    return GetProcessState(jstringToString(env, package_name));
}
extern "C"
JNIEXPORT void JNICALL
Java_moe_ore_servlet_ess_MemoryToolsNative_passGameSafe(JNIEnv *env, jclass clazz) {
    BypassGameSafe();
}

extern "C"
JNIEXPORT jobject JNICALL
Java_moe_ore_servlet_ess_MemoryToolsNative_searchMemory(JNIEnv *env, jclass clazz,
                                                        jint pid, jint range,
                                                        jint type, jstring value) {
    // PACKAGENAME* pm = jstringToString(env, package_name);
    int gs;
    char *v = jstringToString(env, value);
    PMAPS result = MemorySearch(pid, range, v, &gs, type);

    jclass dataClz = env->FindClass("moe/ore/memory/data/PMAP");
    jmethodID dataInit = env->GetMethodID(dataClz, "<init>", "(II)V");


    jclass arraylistClz = env->FindClass("java/util/ArrayList");
    jmethodID listInit = env->GetMethodID(arraylistClz, "<init>", "()V");
    jobject list = env->NewObject(arraylistClz, listInit);

    jmethodID listAdd = env->GetMethodID(arraylistClz, "add", "(Ljava/lang/Object;)Z");

    if (gs != 0 && result != nullptr) {
        for (int i = 0; i < gs; ++i) {
            jobject data = env->NewObject(dataClz, dataInit, result->addr, result->taddr);
            result = result->next;
            env->CallBooleanMethod(list, listAdd, data);
        }
    }

    return list;
}