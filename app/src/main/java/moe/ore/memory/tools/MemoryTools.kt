package moe.ore.memory.tools

import android.content.Context
import com.stericson.RootShell.RootShell
import com.stericson.RootShell.execution.Command
import moe.ore.memory.IRootProcess
import moe.ore.memory.data.PMAP
import moe.ore.memory.data.ProcessInfo
import moe.ore.memory.ext.getLibraryPath
import moe.ore.servlet.LaunchScript
import moe.ore.servlet.Main
import moe.ore.servlet.Main.CODE_MEMORY
import moe.ore.servlet.Main.PROCESS_NAME
import moe.ore.servlet.ipc.RootIPCReceiver

enum class MemoryRange(val i: Int) {
    ALL(0),  //所有内存
    B_BAD(1),  //B内存
    C_ALLOC(2),  //Ca内存
    C_BSS(3),  //Cb内存
    CODE_APP(4),  //XA内存
    C_DATA(5),  //CD内存
    C_HEAP(6),  //Ch内存
    JAVA_HEAP(7),  //Jh内存
    A_ANONMYOUS(8),  //A内存
    CODE_SYSTEM(9),  //Xs内存
    STACK(10),  //S内存
    ASHMEM(11) //As内存
}

enum class SearchType(val i: Int) {
    DWORD(0),
    FLOAT(1),

}

/*
https://man7.org/linux/man-pages/man5/proc.5.html
 */
enum class ProcessState {
    DORMANT, // D 无法中断的休眠状态（通常 IO 的进程）；
    RUNNING, // R 正在运行，在可中断队列中；
    SLEEP, // S 处于休眠状态，静止状态；
    TRACK, // T 停止或被追踪，暂停执行；
    WAIT, // W 进入内存交换（从内核2.6开始无效）；
    DEAD, // X 死掉的进程；
    ZOMBIE, // Z 僵尸进程不存在但暂时无法消除；
    WAKING, // W: 活跃的
    NOBLE,  // <: 高优先级进程
    LOW, // N: 低优先序进程
    LOCK, // L: 有记忆体分页分配并锁在记忆体内 (即时系统或捱A I/O)，即,有些页被锁进内存
    LEADER, // s 进程的领导者（在它之下有子进程）；
    MULTI, // l 多进程的（使用 CLONE_THREAD, 类似 NPTL pthreads）；
    BACKSTAGE // + 位于后台的进程组；
}

object MemoryTools {
    private val lock = Any()
    private val ipcList = ArrayList<IRootProcess>()
    private lateinit var receiver: RootIPCReceiver<IRootProcess>

    private var SelectPid: Int = 0
    private var SearchRange: MemoryRange = MemoryRange.ALL

    fun searchMemory(type: SearchType, value: String): Int {
        lockExec {
            if (SelectPid == 0) {
                throw NotFoundPidException
            }
            return getIpc().searchMemory(SelectPid, SearchRange.i, type.i, value)
        }
        return 0
    }

    fun getResult(start: Int, end: Int): Array<PMAP> {
        lockExec {
            val rs = getIpc().getResults(start, end)
            if (rs != null) {
                return rs
            }
        }
        throw NoSearchResults
    }

    fun setMemoryRange(range: MemoryRange) {
        this.SearchRange = range
    }

    fun getMemoryRange(): MemoryRange {
        return this.SearchRange
    }

    fun selectAppByPid(pid: Int) {
        this.SelectPid = pid
    }

    fun selectAppByPackage(packageName: String) {
        this.SelectPid = getProcessPid(packageName)
    }

    fun getSelectApp(): Int {
        return SelectPid
    }

    fun killGG() {
        lockExec {
            getIpc().killGG()
        }
    }

    fun killXs() {
        lockExec {
            getIpc().killXs()
        }
    }

    fun byPassGameSafe() {
        lockExec { getIpc().passGameSafe() }
    }

    // 仅仅获取正在运行的
    fun getProcessList(): List<ProcessInfo> {
        lockExec {
            return getIpc().processList
        }
        throw UnknownException
    }

    fun getProcessPid(packageName: String): Int {
        var pid = 0
        lockExec {
            pid = getIpc().getProcessPid(packageName)
        }
        if (pid == 0) throw NotFoundPidException
        return pid
    }

    fun getProcessState(packageName: String): ProcessState {
        val state = getIpc().getProcessState(packageName)
        if (state == 0.toChar()) { // 程序没有找到导致无法获取状态
            throw UnknownException
        }
        return when(state) {
            'D', 'd' -> ProcessState.DORMANT
            'R', 'r' -> ProcessState.RUNNING
            'S' -> ProcessState.SLEEP
            'T', 't' -> ProcessState.TRACK
            'W', 'w' -> {
                ProcessState.WAKING
            }
            'X', 'x' -> ProcessState.DEAD
            'Z', 'z' -> ProcessState.ZOMBIE
            '<' -> ProcessState.NOBLE
            'N', 'n' -> ProcessState.LOW
            'L' -> ProcessState.LOCK
            's' -> ProcessState.LEADER
            'l' -> ProcessState.MULTI
            '+' -> ProcessState.BACKSTAGE
            else -> error("unknown state type")
        }
    }

    /*
    下面的方法，与你无关
     */
    private fun getIpc(): IRootProcess = receiver.ipc

    private inline fun lockExec(block: () -> Unit) {
        synchronized(lock) {
            require(ipcList.isNotEmpty()) // 确保已经连接
            block()
        }
    }

    internal fun createIpc(ctx: Context) {
        synchronized(lock) {
            if (!this::receiver.isInitialized) receiver = object: RootIPCReceiver<IRootProcess>(ctx, CODE_MEMORY) {
                override fun onConnect(ipc: IRootProcess) {
                    ipcList.add(ipc)
                }

                override fun onDisconnect(ipc: IRootProcess) {
                    ipcList.remove(ipc)
                    if (ipcList.isEmpty()) {
                        createIpc(ctx) // 尝试重连
                    }
                }
            }
            val libPath = getLibraryPath(ctx, "memory")
            val script = LaunchScript.getLaunchString(ctx, Main::class.java, PROCESS_NAME, CODE_MEMORY, arrayOf(libPath))
            val shell = RootShell.getShell(true)
            shell.add(Command(0, "cd ${ctx.filesDir.absolutePath}"))
            shell.add(Command(1, script))
        }
    }
}