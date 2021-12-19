package moe.ore.servlet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.IBinder;
import android.os.Process;

import java.io.File;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.Objects;

import moe.ore.servlet.ess.MemoryBinder;
import moe.ore.servlet.ess.MemoryToolsNative;
import moe.ore.servlet.ipc.ProcessMain;
import moe.ore.servlet.ipc.RootIPC;
import moe.ore.servlet.reflection.ActivityManager;
import moe.ore.servlet.reflection.ProcessManager;

@SuppressLint("UnsafeDynamicallyLoadedCode")
public class Main extends ProcessMain<Main> {
    /*
    服务所对应的code
     */
    public static final int CODE_MEMORY = 0;
    public static final String PROCESS_NAME = "RootProcess";

    @Override
    public void run(ArrayList<String> args) {
        System.out.println("RootProcess ==> 载入成功");
        try {
            System.out.println("RootProcess ==> 开始初始化服务");

            MemoryToolsNative.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder getBinder(int code) {
        if (code == CODE_MEMORY) {
            return MemoryBinder.INSTANCE;
        }
        return super.getBinder(code);
    }
}