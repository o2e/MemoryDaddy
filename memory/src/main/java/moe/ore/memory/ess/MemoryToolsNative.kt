package moe.ore.memory.ess

object MemoryToolsNative { // 禁止在非Root进程调用
    @JvmStatic external fun killGG()

    @JvmStatic external fun killXs()

    @JvmStatic external fun withPassGameSafe()

    @JvmStatic external fun getPid(packageName: String): Int
    @JvmStatic external fun getProcessState(packageName: String): Char
}