@file:Suppress("SameParameterValue")
package moe.ore.memory.ext

import android.annotation.SuppressLint
import android.text.TextUtils
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception


fun isMiuiSystem(): Boolean {
    return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.code", ""))
}

@SuppressLint("PrivateApi")
private fun getSystemProperty(key: String, value: String): String? {
    return try {
        Class.forName("android.os.SystemProperties")
            .getDeclaredMethod("get", String::class.java, String::class.java)
            .invoke(null, key, value) as String
    } catch (e: Exception) {
        value
    }
}

fun getLinuxKernelsInfo(): String {
    var result = ""
    var line: String
    val cmd = arrayOf("/system/bin/cat", "/proc/version")
    val directory = "/system/bin/"
    try {
        val builder = ProcessBuilder(*cmd)
        builder.directory(File(directory))
        builder.redirectErrorStream(true)
        val process = builder.start()
        val `in`: InputStream = process.inputStream
        val sprout = InputStreamReader(`in`)
        val brought = BufferedReader(sprout, 8 * 1024)
        while (brought.readLine().also { line = it } != null) {
            result += line
        }
        `in`.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}