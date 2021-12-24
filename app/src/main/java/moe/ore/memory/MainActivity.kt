package moe.ore.memory

import android.annotation.SuppressLint
import android.os.Bundle
import moe.ore.memory.databinding.ActivityMainBinding
import moe.ore.memory.ext.toast
import moe.ore.memory.tools.MemoryRange
import moe.ore.memory.tools.MemoryTools
import moe.ore.memory.tools.SearchType
import moe.ore.memory.ui.BaseActivity
import java.lang.StringBuilder

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        consolePrintln("是否初始化成功：" + MemoryTools.init(this)) // 初始化服务

        binding.killGG.setOnClickListener {
            MemoryTools.killGG()
        }

        binding.killXs.setOnClickListener {
            MemoryTools.killXs()
        }

        binding.getPidQq.setOnClickListener {
            consolePrintln("Pid：" + MemoryTools.getProcessPid("com.tencent.mobileqq"))
        }

        binding.qqState.setOnClickListener {
            consolePrintln("State：" + MemoryTools.getProcessState("com.tencent.mobileqq"))
        }

        binding.setRange.setOnClickListener {
            MemoryTools.setMemoryRange(MemoryRange.A_ANONMYOUS)
            consolePrintln("内存范围设置为A")
        }

        binding.getRange.setOnClickListener {
            consolePrintln("Range：" + MemoryTools.getMemoryRange())
        }

        binding.setApp.setOnClickListener {
            MemoryTools.selectAppByPackage("com.tencent.mobileqq")
            consolePrintln("选择QQ主进程")
        }

        binding.getApp.setOnClickListener {
            consolePrintln("App：" + MemoryTools.getSelectApp())
        }

        binding.process.setOnClickListener {
            val builder = StringBuilder()
            // MemoryTools.getProcessList().also { toast(it.size.toString()) }.forEach {
            //    builder.append("包名:${it.name}  名称:${it.processName}  PID:${it.pid}\n")
            //}
            // consolePrintln(builder.toString())
        }

        binding.search.setOnClickListener {
            //val result = MemoryTools.searchMemory(SearchType.DWORD, "1")
            //consolePrintln("结果数量：$result")

        }

        binding.result.setOnClickListener {
            //val result = MemoryTools.getResult(0, 10)
            //result.forEach {
            //    consolePrintln("Addr：${it.addr} ===> TAddr:${it.taddr}")
            //}
        }

        binding.out.setOnLongClickListener {
            binding.out.text = ""
            return@setOnLongClickListener true
        }
    }

    @SuppressLint("SetTextI18n")
    fun consolePrintln(msg: String) {
        runOnUiThread {
            val text = binding.out
            text.text = text.text.toString() + "\n" + msg
        }
    }
}