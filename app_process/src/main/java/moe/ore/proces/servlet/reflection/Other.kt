package moe.ore.proces.servlet.reflection

import android.annotation.SuppressLint
import android.os.Process
import java.lang.Exception
import java.lang.reflect.Method

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