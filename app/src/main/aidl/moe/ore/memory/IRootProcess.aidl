// IRootProcess.aidl
package moe.ore.memory;

import moe.ore.memory.data.ProcessInfo;
import moe.ore.memory.data.PMAP;

interface IRootProcess {
    // 获取Root进程信息
    int getPid();
    int getUid();

    void killGG();
    void killXs();
    void passGameSafe();

    int getProcessPid(String packageName);
    char getProcessState(String packageName);

    List<ProcessInfo> getProcessList();
    int searchMemory(int pid, int range, int type, String value);

    PMAP[] getResults(int start, int end);
}