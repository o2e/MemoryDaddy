package moe.ore.proces.servlet.reflection

import android.annotation.SuppressLint
import android.content.Context
import java.lang.Exception
import java.lang.RuntimeException
import java.lang.reflect.Method
import android.os.Looper

@SuppressLint("StaticFieldLeak", "PrivateApi")
object ActivityManager {
    private val lock = Any()

    private lateinit var oActivityManager: Any
    private lateinit var systemContext: Context

    @JvmStatic fun getSystemContext(): Context {
        synchronized(lock) {
            return try {
                if (this::systemContext.isInitialized) {
                    return systemContext
                }

                // a prepared Looper is required for the calls below to succeed
                if (Looper.getMainLooper() == null) {
                    try {
                        Looper.prepareMainLooper()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                val cActivityThread =
                    Class.forName("android.app.ActivityThread")
                val mSystemMain = cActivityThread.getMethod("systemMain")
                val mGetSystemContext =
                    cActivityThread.getMethod("getSystemContext")
                val oActivityThread = mSystemMain.invoke(null)
                val oContext = mGetSystemContext.invoke(oActivityThread)
                systemContext = oContext as Context
                systemContext
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("unexpected exception in getSystemContext()")
            }
        }
    }

    @JvmStatic fun getActivityManager(): Any {
        // Return object is AIDL interface IActivityManager, not an ActivityManager or
        // ActivityManagerService
        synchronized(lock) {
            if (this::oActivityManager.isInitialized) {
                return oActivityManager
            }
            try { // marked deprecated in Android source
                val cActivityManagerNative = Class.forName("android.app.ActivityManagerNative")
                val mGetDefault: Method = cActivityManagerNative.getMethod("getDefault")
                oActivityManager = mGetDefault.invoke(null)
                return oActivityManager
            } catch (e: Exception) {
                // possibly removed
            }
            try {
                // alternative
                val cActivityManager =
                    Class.forName("android.app.ActivityManager")
                val mGetService: Method = cActivityManager.getMethod("getService")
                oActivityManager = mGetService.invoke(null)
                return oActivityManager
            } catch (e: Exception) {
                e.printStackTrace()
            }
            throw RuntimeException("unable to retrieve ActivityManager")
        }
    }
}