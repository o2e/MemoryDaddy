package moe.ore.memory.tools

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import dalvik.system.BaseDexClassLoader
import java.io.File
import java.lang.RuntimeException
import java.util.*

@SuppressLint("SdCardPath", "ObsoleteSdkInt")
internal fun getLibraryPath(context: Context, libName: String): String { // 获取应用环境对应的二进制库
    var name = libName
    if (Build.VERSION.SDK_INT >= 23) {
        if (context.applicationInfo.flags and ApplicationInfo.FLAG_EXTRACT_NATIVE_LIBS == 0) {
            throw RuntimeException("incompatible with extractNativeLibs=\"false\" in your manifest")
        }
    }
    if (name.lowercase(Locale.getDefault()).startsWith("lib")) {
        name = name.substring(3)
    }
    if (name.lowercase(Locale.getDefault()).endsWith(".so")) {
        name = name.substring(0, name.length - 3)
    }
    val packageName = context.packageName

    // try nativeLibraryDir
    val appInfo: ApplicationInfo = context.applicationInfo
    for (candidate in arrayOf(
        appInfo.nativeLibraryDir + File.separator.toString() + "lib" + name + ".so",
        appInfo.nativeLibraryDir + File.separator.toString() + name + ".so"
    )) {
        if (File(candidate).exists()) {
            return candidate
        }
    }

    // try BaseDexClassLoader
    if (context.classLoader is BaseDexClassLoader) {
        try {
            val bdcl: BaseDexClassLoader = context.classLoader as BaseDexClassLoader
            return bdcl.findLibrary(name)
        } catch (t: Throwable) {
            // not a standard call: catch Errors and Violations too aside from Exceptions
        }
    }

    // try (old) default location
    for (candidate in arrayOf(
        java.lang.String.format(Locale.ENGLISH, "/data/data/%s/lib/lib%s.so", packageName, name),
        java.lang.String.format(Locale.ENGLISH, "/data/data/%s/lib/%s.so", packageName, name)
    )) {
        if (File(candidate).exists()) {
            return candidate
        }
    }
    return ""
}

object NotFoundPidException: RuntimeException("no corresponding application found") // 应用不在运行状态或者未安装

object NoSearchResults: RuntimeException("unknown error")

object UnknownException: RuntimeException("unknown error")