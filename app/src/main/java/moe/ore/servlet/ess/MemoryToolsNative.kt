package moe.ore.servlet.ess

import moe.ore.memory.data.PMAP

object MemoryToolsNative { // 禁止在非Root进程调用
    @JvmStatic external fun killGG()

    @JvmStatic external fun killXs()

    @JvmStatic external fun init()

    @JvmStatic external fun passGameSafe()

    @JvmStatic external fun getPid(packageName: String): Int
    @JvmStatic external fun getProcessState(packageName: String): Char

    @JvmStatic external fun searchMemory(pid: Int, range: Int, type: Int, value: String): List<PMAP>?
}