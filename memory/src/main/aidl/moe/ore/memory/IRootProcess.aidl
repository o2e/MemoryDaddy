// IRootProcess.aidl
package moe.ore.memory;

interface IRootProcess {
    int getPid();
    int getUid();

    void killGG();
    void killXs();
    void passGameSafe();

    int getProcessPid(String packageName);
    char getProcessState(String packageName);


}