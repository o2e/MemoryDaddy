package moe.ore.servlet.reflection

import android.annotation.SuppressLint
import android.os.IBinder
import java.lang.reflect.Method
import android.os.IInterface

import moe.ore.servlet.ipc.RootIPCReceiver
import java.lang.Exception
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import android.content.Intent
import java.lang.RuntimeException


@SuppressLint("DiscouragedPrivateApi", "PrivateApi", "SoonBlockedPrivateApi")
object ServiceManager { // 废弃
    // 每个进程的ServiceManager独享，ServiceManager由系统进程管理
    private var isInit: Boolean = false
    private lateinit var MethodGetService: Method
    private lateinit var MethodAddServiceV1: Method
    private lateinit var MethodAddServiceV2: Method
    private lateinit var MethodAddServiceV3: Method

    @JvmStatic fun getService(name: String): IBinder? {
        init()
        return MethodGetService.invoke(null, name) as? IBinder
    }

    @JvmStatic fun addService(name: String, service: IBinder) {
        init()
        MethodAddServiceV1.invoke(null, name, service)
    }

    @JvmStatic fun addService(name: String, service: IBinder, allowIsolated: Boolean) {
        init()
        MethodAddServiceV2.invoke(null, name, service, allowIsolated)
    }

    @JvmStatic fun addService(name: String, service: IBinder, allowIsolated: Boolean, dumpPriority: Int) {
        init()
        MethodAddServiceV3.invoke(null, name, service, allowIsolated, dumpPriority)
    }

    private fun init() {
        if (!isInit) runCatching {
            val clazz = Class.forName("android.os.ServiceManager")

            MethodGetService = clazz.getMethod("getService", String::class.java)
            MethodAddServiceV1 = clazz.getMethod("addService", String::class.java, IBinder::class.java)
            MethodAddServiceV2 = clazz.getMethod("addService", String::class.java, IBinder::class.java, java.lang.Boolean.TYPE)
            MethodAddServiceV3 = clazz.getMethod("addService", String::class.java, IBinder::class.java, java.lang.Boolean.TYPE, Integer.TYPE)

            isInit = true
        }.onFailure {
            it.printStackTrace()
        }
    }

    private val lock: Any = Any()
    private lateinit var mBroadcastIntent: Method
    private var FLAG_RECEIVER_FROM_SHELL: Int? = null

    @JvmStatic fun sendBroadcast(intent: Intent) {
        try {
            // Prevent system from complaining about unprotected broadcast, if the field exists
            intent.flags = getFlagReceiverFromShell()
            val oActivityManager: Any = ActivityManager.getActivityManager()
            val mBroadcastIntent: Method = getBroadcastIntent(oActivityManager.javaClass)
            if (mBroadcastIntent.parameterTypes.size == 13) {
                // API 24+
                mBroadcastIntent.invoke(oActivityManager, null, intent, null, null, 0, null, null, null, -1, null, false, false, 0)
                return
            }
            if (mBroadcastIntent.parameterTypes.size == 12) {
                // API 21+
                mBroadcastIntent.invoke(oActivityManager, null, intent, null, null, 0, null, null, null, -1, false, false, 0)
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        throw RuntimeException("unable to send broadcast")
    }

    private fun getFlagReceiverFromShell(): Int {
        synchronized(lock) {
            if (FLAG_RECEIVER_FROM_SHELL != null) {
                return FLAG_RECEIVER_FROM_SHELL!!
            }
            try {
                val fFlagReceiverFromShell = Intent::class.java.getDeclaredField("FLAG_RECEIVER_FROM_SHELL")
                FLAG_RECEIVER_FROM_SHELL = fFlagReceiverFromShell.getInt(null)
                return FLAG_RECEIVER_FROM_SHELL!!
            } catch (e: NoSuchFieldException) {
                // not present on all Android versions
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
            FLAG_RECEIVER_FROM_SHELL = 0
            return FLAG_RECEIVER_FROM_SHELL!!
        }
    }

    private fun getBroadcastIntent(cActivityManager: Class<*>): Method {
        synchronized(lock) {
            if (this::mBroadcastIntent.isInitialized) {
                return mBroadcastIntent
            }
            for (m in cActivityManager.methods) {
                if (m.name == "broadcastIntent" && m.parameterTypes.size == 13) {
                    // API 24+
                    mBroadcastIntent = m
                    return mBroadcastIntent
                }
                if (m.name == "broadcastIntent" && m.parameterTypes.size == 12) {
                    // API 21+
                    mBroadcastIntent = m
                    return mBroadcastIntent
                }
            }
            throw RuntimeException("unable to retrieve broadcastIntent method")
        }
    }
}



