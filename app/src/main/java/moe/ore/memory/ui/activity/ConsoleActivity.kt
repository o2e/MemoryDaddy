package moe.ore.memory.ui.activity

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.os.Bundle
import com.stericson.RootShell.RootShell
import com.stericson.RootShell.execution.Command
import moe.ore.memory.IRootProcess
import moe.ore.memory.databinding.ActivityConsoleBinding
import moe.ore.memory.ext.getLibraryPath
import moe.ore.memory.ext.toast
import moe.ore.memory.ui.BaseActivity
import moe.ore.servlet.LaunchScript
import moe.ore.servlet.Main
import moe.ore.servlet.ipc.RootIPCReceiver
import moe.ore.servlet.reflection.ServiceManager
import kotlin.concurrent.thread

class ConsoleActivity: BaseActivity() {
    private lateinit var binding: ActivityConsoleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConsoleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        kotlin.runCatching {
            // test1()
            testProcess()

        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun testProcess() {
        thread {
            if (RootShell.isRootAvailable()) {
                if (!RootShell.isAccessGiven()) {
                    RootShell.getShell(true)
                } else {
                    consolePrintln("等待进程连接")

                    val receiver = object: RootIPCReceiver<IRootProcess>(this, 0) {
                        override fun onConnect(ipc: IRootProcess) {
                            consolePrintln("进程连接成功")
                            println("uid：${ipc.uid}")
                        }

                        override fun onDisconnect(ipc: IRootProcess) {
                            consolePrintln("进程连接断开")
                        }
                    }

                    val libPath = getLibraryPath(this, "memory")
                    consolePrintln("运行路径：${filesDir.absolutePath}")
                    consolePrintln("全支持库路径：${applicationInfo.nativeLibraryDir}")
                    consolePrintln("支持库路径：$libPath")
                    val script = LaunchScript.getLaunchString(this, Main::class.java, Main.PROCESS_NAME, Main.CODE_MEMORY, arrayOf(libPath))
                    consolePrintln("执行脚本：$script")
                    val shell = RootShell.getShell(true)
                    shell.add(Command(0, "cd ${filesDir.absolutePath}"))
                    shell.add(object: Command(1, script) {
                        override fun commandCompleted(id: Int, exitcode: Int) {
                            consolePrintln("commandCompleted($id, $exitcode)")
                        }

                        override fun commandOutput(id: Int, line: String?) {
                            consolePrintln("commandOutput($id, $line)")
                        }

                        override fun commandTerminated(id: Int, reason: String?) {
                            consolePrintln("commandTerminated($id, $reason)")
                        }
                    })

                }
            } else toast("设备不支持Root")
        }
    }

    @SuppressLint("SetTextI18n")
    fun consolePrintln(msg: String) {
        runOnUiThread {
            val text = binding.textConsole
            text.text = text.text.toString() + "\n" + msg
        }
    }
}