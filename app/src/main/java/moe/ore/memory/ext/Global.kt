package moe.ore.memory.ext

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Process
import java.lang.Exception
import java.lang.reflect.Method

const val isDebugConsole: Boolean = false // 是否处于调试模式

val uiHandler: Handler = Handler(Looper.getMainLooper().let {
    if (it != null) Looper.getMainLooper() else {
        Looper.prepareMainLooper()
        return@let it
    }
})

@SuppressLint("SoonBlockedPrivateApi")
fun isVmDebugEnabled(): Boolean {
    return if (Process.myUid() >= 10000) {
        false
    } else {
        try {
            val cVMDebug = Class.forName("dalvik.system.VMDebug")
            val mIsDebuggingEnabled: Method = cVMDebug.getDeclaredMethod("isDebuggingEnabled")
            mIsDebuggingEnabled.invoke(null) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}