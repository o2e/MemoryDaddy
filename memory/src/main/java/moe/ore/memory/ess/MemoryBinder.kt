package moe.ore.memory.ess

import android.os.Process.myPid
import android.os.Process.myUid
import moe.ore.memory.IRootProcess
import moe.ore.memory.ess.MemoryToolsNative.getPid

internal object MemoryBinder: IRootProcess.Stub() {
    override fun getPid(): Int {
        return myPid()
    }

    override fun getUid(): Int {
        return myUid()
    }

    override fun killGG() {
        MemoryToolsNative.killGG()
    }

    override fun killXs() {
        MemoryToolsNative.killXs()
    }

    override fun passGameSafe() {
        MemoryToolsNative.withPassGameSafe()
    }

    override fun getProcessPid(packageName: String): Int {
        return getPid(packageName)
    }

    override fun getProcessState(packageName: String): Char {
        return MemoryToolsNative.getProcessState(packageName)
    }

    /*
    override fun getProcessList(): List<ProcessInfo> {
        val processes = ArrayList<ProcessInfo>()
        File("/proc").listFiles()!!.forEach { process ->
            File(process.absolutePath + "/cmdline").let {
                if (it.exists()) {
                    val name = File(process.absolutePath + "/cmdline").readText()
                    if (name.isNotBlank()) {
                        val processName = File(process.absolutePath + "/comm").readText()
                        val pid = getPid(name)
                        if (pid >= Process.FIRST_APPLICATION_UID && pid != getPid())
                            processes.add(ProcessInfo(name, processName, pid))
                    }
                }
            }
        }
        return processes
    }

    override fun searchMemory(pid: Int, range: Int, type: Int, value: String): Int {
        // println("开始搜索：$pid: Int, $range: Int, $type: Int, $value: String")
        var result = MemoryToolsNative.searchMemory(pid, range, type, value)
        if (result == null) {
            result = emptyList()
        }
        CACHE_SEARCH = result
        // println("搜索结果：${result.size}")
        return CACHE_SEARCH.size
    }

    override fun getResults(start: Int, end: Int): Array<PMAP>? {
        return if (this::CACHE_SEARCH.isInitialized)
            CACHE_SEARCH.subList(start, end).toTypedArray()
        else
            null
    }*/

}