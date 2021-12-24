package moe.ore.proces.servlet.reflection

import android.annotation.SuppressLint
import java.lang.Exception
import java.lang.reflect.Method

@SuppressLint("DiscouragedPrivateApi", "PrivateApi")
object ProcessManager {
    @JvmStatic fun setAppName(name: String, userId: Int) {
        try {
            val cDdmHandleAppName = Class.forName("android.ddm.DdmHandleAppName")
            val m: Method = cDdmHandleAppName.getDeclaredMethod(
                "setAppName",
                String::class.java,
                Int::class.javaPrimitiveType
            )
            m.invoke(null, name, userId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

