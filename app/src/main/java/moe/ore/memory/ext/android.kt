package moe.ore.memory.ext

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Looper
import android.widget.Toast
import dalvik.system.BaseDexClassLoader
import java.io.File
import java.lang.RuntimeException
import java.util.*

fun Context.toast(msg: String) {
    if (canChangeUi()) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    } else {
        uiHandler.post {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}

fun canChangeUi() = Looper.myLooper() == Looper.getMainLooper()

@SuppressLint("SdCardPath", "ObsoleteSdkInt")
fun getLibraryPath(context: Context, libname: String): String? { // 获取应用环境对应的二进制库
    var libName = libname
    if (Build.VERSION.SDK_INT >= 23) {
        if (context.applicationInfo.flags and ApplicationInfo.FLAG_EXTRACT_NATIVE_LIBS == 0) {
            throw RuntimeException("incompatible with extractNativeLibs=\"false\" in your manifest")
        }
    }
    if (libName.lowercase(Locale.getDefault()).startsWith("lib")) {
        libName = libName.substring(3)
    }
    if (libName.lowercase(Locale.getDefault()).endsWith(".so")) {
        libName = libName.substring(0, libName.length - 3)
    }
    val packageName = context.packageName

    // try nativeLibraryDir
    val appInfo: ApplicationInfo = context.applicationInfo
    for (candidate in arrayOf(
        appInfo.nativeLibraryDir + File.separator.toString() + "lib" + libName + ".so",
        appInfo.nativeLibraryDir + File.separator.toString() + libName + ".so"
    )) {
        if (File(candidate).exists()) {
            return candidate
        }
    }

    // try BaseDexClassLoader
    if (context.classLoader is BaseDexClassLoader) {
        try {
            val bdcl: BaseDexClassLoader = context.classLoader as BaseDexClassLoader
            return bdcl.findLibrary(libName)
        } catch (t: Throwable) {
            // not a standard call: catch Errors and Violations too aside from Exceptions
        }
    }

    // try (old) default location
    for (candidate in arrayOf(
        java.lang.String.format(Locale.ENGLISH, "/data/data/%s/lib/lib%s.so", packageName, libName),
        java.lang.String.format(Locale.ENGLISH, "/data/data/%s/lib/%s.so", packageName, libName)
    )) {
        if (File(candidate).exists()) {
            return candidate
        }
    }
    return null
}