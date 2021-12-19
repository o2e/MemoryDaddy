package moe.ore.servlet;

import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Policies {
    /**
     * SELinux policies that require patching for the Binder calls to work on newer Android
     * versions. Failing to do this may cause Binder transactions to fail.
     */
    private static String[] required = new String[] {
            /* We skip the init context used in older SuperSU versions, as that is potentially
               dangerous, and Android versions that actually require this policy modification
               are likely to run a SuperSU version that uses it's own SELinux context or Magisk */
            "allow appdomain supersu binder { call transfer }",
            "allow appdomain magisk binder { call transfer }"
    };

    /**
     * We only want to patch the SELinux policies once, keep track
     */
    private static Boolean patched = false;

    /**
     * Sets SELinux policies patched state.<br>
     * <br>
     * By default policies are only patched once. You can trigger the script to include the
     * policy patches again once by passing false, or every time by passing null. If you pass
     * true, the policies will not be patched.<br>
     * <br>
     * If you are not using the Binder IPC calls, you may want to set it to true to prevent
     * the policies from being needlessly patched.
     *
     * @param value New policy patched state
     */
    public static void setPatched(Boolean value) {
        patched = value;
    }

    /**
     * Create script to patch SELinux policies.
     *
     * @param preLaunch List that retrieves commands to execute to perform the policy patch
     */
    public static void getPatch(List<String> preLaunch) {
        if ((patched == null) || !patched) {
            StringBuilder command = new StringBuilder("supolicy --live");
            for (String policy : required) {
                command.append(" \"").append(policy).append("\"");
            }
            command.append(" >/dev/null 2>/dev/null");
            preLaunch.add(command.toString());
            if (patched != null) {
                patched = true;
            }
        }
    }
}