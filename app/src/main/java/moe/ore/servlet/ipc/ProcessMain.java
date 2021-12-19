package moe.ore.servlet.ipc;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.Process;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Objects;

import moe.ore.servlet.reflection.ActivityManager;
import moe.ore.servlet.reflection.ProcessManager;

@SuppressWarnings("unchecked")
@SuppressLint("UnsafeDynamicallyLoadedCode")
public class ProcessMain<T extends ProcessMain<T>> {
    public static String PROCESS_NAME = "RootProcess";

    private final Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();

    public ProcessMain() {
        Thread.setDefaultUncaughtExceptionHandler(this::caughtException);

        ActivityManager.getActivityManager();
        ActivityManager.getSystemContext();
    }

    // 获取服务
    public IBinder getBinder(int code) {
        return null;
    }

    // 执行运行
    public void run(ArrayList<String> args) {

    }

    // 错误捕捉
    public void caughtException(Thread thread, Throwable throwable) {
        System.out.println("RootProcess ==> " + throwable);
        if (oldHandler != null) {
            oldHandler.uncaughtException(thread, throwable);
        } else {
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        System.out.println("RootProcess ==> 进程加载成功");

        String mainClass = ProcessMain.class.getName();
        String packageName = null;
        int bindCode = 0;

        ArrayList<String> processArgs = new ArrayList<>();
        for (String arg : args) {
            if (arg.startsWith("--lib-path=")) {
                String libPath = arg.substring(11);
                loadLib(libPath);
            } else if (arg.startsWith("--process-name=")) {
                ProcessMain.PROCESS_NAME = arg.substring(15);
            } else if (arg.startsWith("--main-class=")) {
                mainClass = arg.substring(13);
            } else if (arg.startsWith("--bind-code=")) {
                bindCode = Integer.parseInt(arg.substring(12));
            } else if (arg.startsWith("--package-name=")) {
                packageName = arg.substring(15);
            }
            else {
                processArgs.add(arg);
            }
        }

        int uid = Process.myUid();
        int userId = uid / 100000;

        ProcessManager.setAppName(PROCESS_NAME, userId);

        System.out.println("Is64Bit：" + Process.is64Bit());
        System.out.println("Uid：" + uid);
        System.out.println("Tid：" + Process.myTid());
        System.out.println("Pid：" + Process.myPid());
        System.out.println("UserHandle：" + Process.myUserHandle());
        System.out.println("MainClass：" + mainClass);
        System.out.println("BindCode：" + bindCode);
        System.out.println("Package：" + packageName);

        try {
            if (packageName == null) {
                System.out.println("package-name must not null");
                return;
            }
            Class<ProcessMain<?>> clazz = (Class<ProcessMain<?>>) Class.forName(mainClass);
            // Constructor<ProcessMain<?>> constructor = clazz.getDeclaredConstructor();
            ProcessMain<?> obj = clazz.newInstance();
            obj.run(processArgs);

            try {
                new RootIPC(packageName, obj.getBinder(bindCode), bindCode, 30 * 1000, true);
            } catch (RootIPC.TimeoutException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void loadLib(String path) {
        if (path.startsWith("/") && (path.endsWith(".so"))) {
            System.out.println("Loading " + path);
            System.load(path);
        } else {
            File file = new File(path);
            if (file.isDirectory()) for (File f: Objects.requireNonNull(file.listFiles())) {
                loadLib(f.getAbsolutePath());
            }
        }
    }
}
