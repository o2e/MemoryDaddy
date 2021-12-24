#include <jni.h>
#include <string>

#include "MemoryTools.h"
#include "JniHelper.h"

extern "C" { // 信息类root操作
    JNIEXPORT jint JNICALL Java_moe_ore_memory_ess_MemoryToolsNative_getPid(JNIEnv *env, jclass clazz, jstring package_name) {
        int pid = getPID(jstringToString(env, package_name));
        return (jint) pid;
    }

    JNIEXPORT jchar JNICALL Java_moe_ore_memory_ess_MemoryToolsNative_getProcessState(JNIEnv *env, jclass clazz, jstring package_name) {
        return GetProcessState(jstringToString(env, package_name));
    }


}

extern "C" { // 辅助操作性功能
    JNIEXPORT void JNICALL Java_moe_ore_memory_ess_MemoryToolsNative_killGG(JNIEnv *env, jclass clazz) {
        // killGG();
        //在/data/data/[GG修改器包名]/files/里面有一个文件夹名字是GG-****
        //如果有这个文件夹，就获取上面所说的包名，杀掉GG修改器
        DIR *dir;
        DIR *dirGG = nullptr;
        struct dirent *ptr;
        struct dirent *ptrGG;
        char filepath[256];
        dir = opendir("/data/data"); // 打开路径
        if (dir != nullptr) {
            while ((ptr = readdir(dir)) != nullptr)    // 循环读取路径下的每一个文件/文件夹
            {
                // 如果读取到的是"."或者".."则跳过，读取到的不是文件夹名字也跳过
                if ((strcmp(ptr->d_name, ".") == 0) || (strcmp(ptr->d_name, "..") == 0)) continue;
                if (ptr->d_type != DT_DIR) continue;
                sprintf(filepath, "/data/data/%s/files", ptr->d_name);
                dirGG = opendir(filepath);    // 打开文件
                if (dirGG != nullptr) {
                    while ((ptrGG = readdir(dirGG)) != nullptr) {
                        if ((strcmp(ptrGG->d_name, ".") == 0) || (strcmp(ptr->d_name, "..") == 0))
                            continue;
                        if (ptrGG->d_type != DT_DIR)
                            continue;
                        if (strstr(ptrGG->d_name, "GG")) {
                            int pid;//pid
                            pid = getPID(ptr->d_name);//获取GG包名
                            //ptr->d_name存储文件名字(也就是软件包名)
                            if (pid == 0)//如果pid是0，代表GG没有运行
                                continue;
                            else //如果成功获取pid
                                killProcessByOs(ptr->d_name);
                        }
                    }
                }
            }
        }
        closedir(dir);
        closedir(dirGG);
    }

    JNIEXPORT void JNICALL Java_moe_ore_memory_ess_MemoryToolsNative_withPassGameSafe(JNIEnv *env, jclass clazz) {
        system("echo 0 > /proc/sys/fs/inotify/max_user_watches");
    }

    JNIEXPORT void JNICALL Java_moe_ore_memory_ess_MemoryToolsNative_killXs(JNIEnv *env, jclass clazz) {
        killXs();
    }

}

/*
extern "C"
JNIEXPORT jobject JNICALL
Java_moe_ore_memory_ess_MemoryToolsNative_searchMemory(JNIEnv *env, jclass clazz,
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
*/
