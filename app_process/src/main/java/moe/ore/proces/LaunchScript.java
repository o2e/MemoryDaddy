package moe.ore.proces;

import static android.system.Os.getenv;

import static moe.ore.proces.servlet.reflection.OtherKt.isVmDebugEnabled;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.SystemClock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LaunchScript {
    public static final String LOG_FILE = "run.log";

    /**
     * Get string to be executed (in a root shell) to launch the Java code as root.
     *
     * @param context Application or activity context
     * @param clazz Class containing "main" method
     * @param niceName Process name to use (ps) instead of app_process (should be unique to your app), or null
     * @param code Give a unique id to the root process being pulled up
     * @param libs Binary libraries that need to be loaded in the root process
     * @param params Parameters to supply to Java code, or null
     * @return Script
     */
    public static String getLaunchString(Context context, Class<?> clazz, String niceName, int code, String[] libs, String... params) {
        String app_process = AppProcess.getAppProcess();
        String main_class = clazz.getName();

        ArrayList<String> args = new ArrayList<>();
        for (String lib : libs) {
            args.add(String.format("--lib-path=%s", lib));
        }
        args.add(String.format("--process-name=%s", niceName));
        args.add(String.format("--main-class=%s", main_class));
        args.add(String.format("--bind-code=%s", code));
        args.add(String.format("--package-name=%s", context.getPackageName()));
        args.addAll(Arrays.asList(params));

        return getLaunchString(context.getPackageCodePath(), main_class, app_process, AppProcess.guessIfAppProcessIs64Bits(app_process), args.toArray(new String[0]), niceName);
    }

    /**
     * Get string to be executed (in a root shell) to launch the Java code as root.
     *
     * You would normally use {@link #getLaunchScript(Context, Class, String, String, String[], String)}
     *
     * @param context Application or activity context
     * @param clazz Class containing "main" method
     * @param app_process Specific app_process binary to use, or null for default
     * @param params Parameters to supply to Java code, or null
     * @param niceName Process name to use (ps) instead of app_process (should be unique to your app), or null
     * @return Script
     */
    private static String getLaunchString(Context context, Class<?> clazz, String app_process, String[] params, String niceName) {
        if (app_process == null) app_process = AppProcess.getAppProcess();
        return getLaunchString(context.getPackageCodePath(), clazz.getName(), app_process, AppProcess.guessIfAppProcessIs64Bits(app_process), params, niceName);
    }

    /**
     * Get string to be executed (in a root shell) to launch the Java code as root.
     *
     * You would normally use {@link #getLaunchScript(Context, Class, String, String, String[], String)}
     *
     * @param packageCodePath Path to APK
     * @param clazz Class containing "main" method
     * @param app_process Specific app_process binary to use
     * @param is64Bit Is specific app_process binary 64-bit?
     * @param params Parameters to supply to Java code, or null
     * @param niceName Process name to use (ps) instead of app_process (should be unique to your app), or null
     * @return Script
     */
    private static String getLaunchString(String packageCodePath, String clazz, String app_process, boolean is64Bit, String[] params, String niceName) {
        String ANDROID_ROOT = System.getenv("ANDROID_ROOT");
        StringBuilder prefix = new StringBuilder();
        if (ANDROID_ROOT != null) {
            prefix.append("ANDROID_ROOT=");
            prefix.append(ANDROID_ROOT);
            prefix.append(' ');
        }

        int p;
        String[] extraPaths = null;
        if ((p = app_process.lastIndexOf('/')) >= 0) {
            extraPaths = new String[] { app_process.substring(0, p) };
        }
        String LD_LIBRARY_PATH = getPatchedLdLibraryPath(is64Bit, extraPaths);
        if (LD_LIBRARY_PATH != null) {
            prefix.append("LD_LIBRARY_PATH=");
            prefix.append(LD_LIBRARY_PATH);
            prefix.append(' ');
        }

        String vmParams = "";
        String extraParams = "";
        if (niceName != null) {
            extraParams += " --nice-name=" + niceName;
        }
        if (isVmDebugEnabled()) { // we don't use isEnabled() because that has a different meaning when called as root, and though rare we might call this method from root too
            vmParams += " -Xcompiler-option --debuggable";
            if (Build.VERSION.SDK_INT >= 28) {
                // Android 9.0 Pie changed things up a bit
                vmParams += " -XjdwpProvider:internal -XjdwpOptions:transport=dt_android_adb,suspend=n,server=y";
            } else {
                vmParams += " -agentlib:jdwp=transport=dt_android_adb,suspend=n,server=y";
            }
        }
        String ret = String.format("NO_ADDR_COMPAT_LAYOUT_FIXUP=1 %sCLASSPATH=%s nohup %s%s /system/bin%s %s", prefix.toString(), packageCodePath, app_process, vmParams, extraParams, clazz);
        if (params != null) {
            StringBuilder full = new StringBuilder(ret);
            for (String param : params) {
                full.append(' ');
                full.append(param);
            }
            ret = full.toString();
        }
        return String.format("%s > %s 2>&1 &", ret, LOG_FILE);
    }

    @TargetApi(23)
    private static boolean haveLinkerNamespaces() {
        return (
                (Build.VERSION.SDK_INT >= 24) ||

                        // 7.0 preview
                        ((Build.VERSION.SDK_INT == 23) && (Build.VERSION.PREVIEW_SDK_INT != 0))
        );
    }

    private static String getPatchedLdLibraryPath(boolean use64bit, String[] extraPaths) {
        String LD_LIBRARY_PATH = getenv("LD_LIBRARY_PATH");
        if (!haveLinkerNamespaces()) {
            if (LD_LIBRARY_PATH != null) {
                // some firmwares have this, some don't, launch at boot may fail without, or with,
                // so just copy what is the current situation
                return LD_LIBRARY_PATH;
            }
            return null;
        } else {
            StringBuilder paths = new StringBuilder();

            // these default paths are taken from linker code in AOSP, and are normally used
            // when LD_LIBRARY_PATH isn't set explicitly
            String[] scan;
            if (use64bit) {
                scan = new String[]{
                        "/system/lib64",
                        "/data/lib64",
                        "/vendor/lib64",
                        "/data/vendor/lib64"
                };
            } else {
                scan = new String[]{
                        "/system/lib",
                        "/data/lib",
                        "/vendor/lib",
                        "/data/vendor/lib"
                };
            }

            for (String path : scan) {
                File file = (new File(path));
                if (file.exists()) {
                    try {
                        paths.append(file.getCanonicalPath());
                        paths.append(':');

                        // This part can trigger quite a few SELinux policy violations, they
                        // are harmless for our purpose, but if you're trying to trace SELinux
                        // related issues in your Binder calls, you may want to comment this part
                        // out. It is rarely (but still sometimes) actually required for your code
                        // to run.

                        File[] files = file.listFiles();
                        if (files != null) {
                            for (File dir : files) {
                                if (dir.isDirectory()) {
                                    paths.append(dir.getCanonicalPath());
                                    paths.append(':');
                                }
                            }
                        }
                    } catch (IOException e) {
                        // failed to resolve canonical path
                    }
                }
            }

            if (extraPaths != null) {
                for (String path : extraPaths) {
                    paths.append(path);
                    paths.append(':');
                }
            }

            paths.append("/librootjava"); // for detection

            if (LD_LIBRARY_PATH != null) {
                paths.append(':');
                paths.append(LD_LIBRARY_PATH);
            }

            return paths.toString();
        }
    }

    /**
     * Get script to be executed (in a root shell) to launch the Java code as root.<br>
     * <br>
     * app_process is relocated during script execution. If a relocate_path is supplied
     * it must already exist. It is also made linker-namespace-safe, so optionally you
     * can put native libraries there (rarely necessary). By default we relocate to the app's
     * cache dir, falling back to /dev in case of issues or the app living on external storage.<br>
     * <br>
     * Note that SELinux policy patching takes place only in the script returned from
     * the first call, so be sure to execute that script first if you call this method
     * multiple times. You can change this behavior with the {@link Policies#setPatched(Boolean)}
     * method. The patch is only needed for the Binder-based IPC calls, if you do not use those,
     * you may consider passing true to {@link Policies#setPatched(Boolean)} and prevent the
     * patching altogether.
     *
     * @param context Application or activity context
     * @param clazz Class containing "main" method
     * @param app_process Specific app_process binary to use, or null for default
     * @param relocate_path Path to relocate app_process to (must exist), or null for default
     * @param params Parameters to supply to Java code, or null
     * @param niceName Process name to use (ps) instead of app_process (should be unique to your app), or null
     * @return Script
     */
    private static List<String> getLaunchScript(Context context, Class<?> clazz, String app_process, String relocate_path, String[] params, String niceName) {
        ArrayList<String> pre = new ArrayList<String>();
        ArrayList<String> post = new ArrayList<String>();

        // relocate app_process
        app_process = AppProcess.getAppProcessRelocate(context, app_process, pre, post, relocate_path);

        // librootjavadaemon uses this
        pre.add(0, "#app_process=" + app_process);

        // patch SELinux policies
        Policies.getPatch(pre);

        // combine
        ArrayList<String> script = new ArrayList<String>(pre);
        script.add(getLaunchString(context, clazz, app_process, params, niceName));
        script.addAll(post);
        return script;
    }

    /** Prefixes of filename to remove from the app's cache directory */
    private static final String[] CLEANUP_CACHE_PREFIXES = new String[] { ".app_process32_", ".app_process64_" };

    /**
     * Clean up leftover files from our cache directory.<br>
     * <br>
     * In ideal circumstances no files should be left dangling, but in practise it happens sooner
     * or later anyway. Periodically (once per app launch or per boot) calling this method is
     * advised.<br>
     * <br>
     * This method should be called from a background thread, as it performs disk i/o.<br>
     * <br>
     * It is difficult to determine which of these files may actually be in use, especially in
     * daemon mode. We try to determine device boot time, and wipe everything from before that
     * time. For safety we explicitly keep files using our current UUID.
     *
     * @param context Context to retrieve cache directory from
     */
    public static void cleanupCache(Context context) {
        cleanupCache(context, CLEANUP_CACHE_PREFIXES);
    }

    /**
     * Clean up leftover files from our cache directory.<br>
     * <br>
     * This version is for internal use, see {@link #cleanupCache(Context)} instead.
     *
     * @param context Context to retrieve cache directory from
     * @param prefixes List of prefixes to scrub
     */
    private static void cleanupCache(Context context, final String[] prefixes) {
        try {
            File cacheDir = context.getCacheDir();
            if (cacheDir.exists()) {
                // determine time of last boot
                long boot = System.currentTimeMillis() - SystemClock.elapsedRealtime();

                // find our files
                for (File file : Objects.requireNonNull(cacheDir.listFiles((dir, name) -> {
                    boolean accept = false;
                    for (String prefix : prefixes) {
                        // just in case: don't return files that contain our current uuid
                        if (name.startsWith(prefix) && !name.endsWith(AppProcess.UUID)) {
                            accept = true;
                            break;
                        }
                    }
                    return accept;
                }))) {
                    if (file.lastModified() < boot) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
