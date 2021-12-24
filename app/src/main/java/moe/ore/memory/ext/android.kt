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
