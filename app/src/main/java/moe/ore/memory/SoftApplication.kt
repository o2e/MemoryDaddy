package moe.ore.memory

import android.app.Application
import moe.ore.memory.tools.MemoryTools
import moe.ore.servlet.LaunchScript

class SoftApplication: Application() {
    private external fun initNative()

    init {
        System.loadLibrary("memory")
    }

    override fun onCreate() {
        super.onCreate()

        initNative()

        LaunchScript.cleanupCache(this)
        MemoryTools.createIpc(this)
    }

}